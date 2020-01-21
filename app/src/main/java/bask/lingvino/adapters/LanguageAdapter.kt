package bask.lingvino.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import bask.lingvino.R
import bask.lingvino.models.LanguageItem
import kotlinx.android.synthetic.main.language_spinner_item.view.*

class LanguageAdapter(context: Context, items: MutableList<LanguageItem>) :
    ArrayAdapter<LanguageItem>(context, 0, items) {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val vh: LanguageViewHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.language_spinner_item, parent, false)
            vh = LanguageViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as LanguageViewHolder
        }

        vh.bindItems(getItem(position)!!)

        return view
    }

    private inner class LanguageViewHolder(v: View) {
        private val view: View = v

        fun bindItems(language: LanguageItem) {
            view.flagIV.setImageResource(language.flag)
            view.languageTV.text = language.languageName
        }
    }
}