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
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.android.synthetic.main.calendarviewheader.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class CalendarViewFragment: Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var wotdDates: MutableList<LocalDate>
    private var selectedDate: LocalDate? = null

    companion object {
        private const val argKey = "wotdDates"
        fun newInstance(dates: Array<String>): CalendarViewFragment {
            val args = Bundle().apply { putStringArray(argKey, dates) }
            return CalendarViewFragment().apply { arguments = args }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getStringArray("wotdDates").let {
            wotdDates = it?.map { dateString ->
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("d-M-yyyy"))
            }!!.toMutableList()
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
        calendarView.setup(wotdDates.first().yearMonth, YearMonth.now(), DayOfWeek.MONDAY)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound.
            lateinit var day: CalendarDay

            val textView = with(view) {
                setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate == day.date) {
                            selectedDate = null
                            calendarView.notifyDayChanged(day)
                        } else {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            calendarView.notifyDateChanged(day.date)
                            oldDate?.let { calendarView.notifyDateChanged(oldDate) }
                        }
                    }
                }
                return@with this as TextView
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    when (day.date) {
                        !in wotdDates -> {
                            textView.setTextColor(
                                ContextCompat.getColor(context!!, R.color.disabled)
                            )
                            textView.background = null
                        }
                        selectedDate -> {
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