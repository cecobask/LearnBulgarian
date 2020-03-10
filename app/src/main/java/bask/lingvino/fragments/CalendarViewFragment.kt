package bask.lingvino.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignTop
import kotlinx.android.synthetic.main.calendarviewday.view.*
import kotlinx.android.synthetic.main.calendarviewheader.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
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
        val sourceLang = sharedPref.getString("SOURCE_LANG_NAME", "English")!!
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
                    },
                    when (sourceLang) {
                        "Bulgarian" -> it.wordBG
                        "English" -> it.wordEN
                        "Russian" -> it.wordRU
                        else -> it.wordES
                    }
                )
            }.reversed()
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
            calendarWords.first().date.yearMonth,
            calendarWords.last().date.yearMonth, DayOfWeek.MONDAY
        )
        calendarView.scrollToMonth(calendarWords.last().date.yearMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound.
            lateinit var day: CalendarDay
            val calendarDay = view.calendarDay
            val calendarWOTD = view.calendarWOTD
            val cLayout = with(view.calendarDayLayout) {
                // Open WordOfTheDay for clicked date.
                setOnClickListener {
                    if (day.date in calendarWords.map { it.date }) {
                        val wotdFragment = WordOfTheDayFragment.newInstance(
                            day.date.format(DateTimeFormatter.ofPattern("d-M-yyyy"))
                        )
                        fragmentManager!!
                            .beginTransaction()
                            .replace(
                                R.id.fragmentContainer,
                                wotdFragment,
                                wotdFragment.javaClass.simpleName
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
                            setText(calendarWords.find { it.date == day.date }!!.sourceWord)
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
                return@with this
            }
        }

        // Controls each date from the calendar.
        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val calendarDay = container.calendarDay
                val calendarWOTD = container.calendarWOTD
                val cLayout = container.cLayout

                // Display day and WOTD values.
                calendarDay.text = day.date.dayOfMonth.toString()
                calendarWOTD.text = calendarWords.find { it.date == day.date }?.targetWord

                // Make sure dates from different months are not shown on the same line.
                if (day.owner == DayOwner.THIS_MONTH) {
                    cLayout.visibility = View.VISIBLE
                    when (day.date) {
                        // The date is not part of the 'calendarWords' list.
                        !in calendarWords.map { it.date } -> {
                            calendarDay.setTextColor(
                                ContextCompat.getColor(context!!, R.color.disabled)
                            )
                            calendarDay.background = null
                        }
                        // The date is today.
                        today -> {
                            calendarDay.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                            calendarDay.setBackgroundResource(R.drawable.selected_date)
                        }
                        // The date has a WOTD value.
                        else -> {
                            calendarDay.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
                            calendarDay.setTypeface(null, Typeface.BOLD)
                            calendarDay.background = null
                        }
                    }
                } else {
                    cLayout.visibility = View.INVISIBLE
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