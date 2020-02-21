package bask.lingvino.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.models.CalendarWord
import bask.lingvino.models.WordOfTheDay
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.skydoves.balloon.*
import kotlinx.android.synthetic.main.calendarviewheader.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

class CalendarViewFragment: Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var calendarWords: List<CalendarWord>
    private var today: LocalDate = LocalDate.now()

    companion object {
        private const val argKey = "wordObjects"
        fun newInstance(wotdsJSON: String): CalendarViewFragment {
            // Receive JSON array of WordOfTheDay objects.
            val args = Bundle().apply { putString(argKey, wotdsJSON) }
            return CalendarViewFragment().apply { arguments = args }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)
        val targetLang = sharedPref.getString("TARGET_LANG_NAME", "Bulgarian")!!
        arguments?.getString("wordObjects").let { json ->
            val words: MutableList<WordOfTheDay> =
                Gson().fromJson(json, object : TypeToken<List<WordOfTheDay>>() {}.type)
            calendarWords = words.map {
                // Create object with wotdDate (string) parsed as LocalDate of specified format
                // and determine value for word, based on target language.
                CalendarWord(
                    LocalDate.parse(it.wordDate, DateTimeFormatter.ofPattern("d-M-yyyy")),
                    when (targetLang) {
                        "Bulgarian" -> it.wordBG
                        "English" -> it.wordEN
                        "Russian" -> it.wordRU
                        else -> it.wordES
                    }
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.calendarview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        calendarView.setup(
            calendarWords.first().date.yearMonth, calendarWords.last().date.yearMonth, DayOfWeek.MONDAY
        )

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound.
            lateinit var day: CalendarDay

            val textView = with(view) {
                // Open WordOfTheDay for clicked date.
                setOnClickListener {
                    if (day.date in calendarWords.map { it.date }) {
                        fragmentManager!!
                            .beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                WordOfTheDayFragment.newInstance(
                                    day.date.format(DateTimeFormatter.ofPattern("d-M-yyyy"))
                                )
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                }

                // Show tooltip with word value for long clicked date.
                setOnLongClickListener {
                    // Check if there is a word from that day.
                    if (day.date in calendarWords.map { it.date }) {
                        createBalloon(context!!) {
                            setArrowOrientation(ArrowOrientation.BOTTOM)
                            setArrowPosition( // Align arrow position, based day of the week.
                                when (day.date.dayOfWeek.toString()) {
                                    "MONDAY" -> 0.15f
                                    "TUESDAY" -> 0.45f
                                    "SATURDAY" -> 0.55f
                                    "SUNDAY" -> 0.85f
                                    else -> 0.5f
                                }
                            )
                            setWidthRatio(0.5f)
                            setHeight(70)
                            setAlpha(1f)
                            setText(calendarWords.find { it.date == day.date }!!.text)
                            setTextSize(18f)
                            setTextTypeface(Typeface.BOLD)
                            setTextColorResource(R.color.white)
                            setBackgroundColorResource(R.color.colorPrimaryDark)
                            setBalloonAnimation(BalloonAnimation.FADE)
                            setShowTime(1)
                            setAutoDismissDuration(1500)
                            showAlignTop(this.build())
                        }
                    }
                    true
                }
                return@with this as TextView
            }
        }

        // Controls each date from the calendar.
        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                // Make sure dates from different months are not shown on the same line.
                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    when (day.date) {
                        !in calendarWords.map { it.date } -> {
                            textView.setTextColor(
                                ContextCompat.getColor(context!!, R.color.disabled)
                            )
                            textView.background = null
                        }
                        today -> {
                            textView.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                            textView.setBackgroundResource(R.drawable.selected_date)
                        }
                        else -> {
                            textView.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                            textView.setTypeface(null, Typeface.BOLD)
                            textView.background = null
                        }
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.calendarViewHeaderText
        }

        // Controls the header for each month.
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Suppress concatenation warning for `setText` call.
                container.textView.text =
                    "${month.yearMonth.month.name.toLowerCase().capitalize()} ${month.year}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.wotdCalendar)
    }

}