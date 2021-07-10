package de.threateningcodecomments.adapters

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter


class DropdownArrayAdapter<T>(context: Context, textViewResourceId: Int,
                              objects: List<T>) : ArrayAdapter<T>(context, textViewResourceId, objects) {
    private val filter: Filter = KNoFilter()
    var items: List<T>
    override fun getFilter(): Filter {
        return filter
    }

    private inner class KNoFilter : Filter() {
        protected override fun performFiltering(arg0: CharSequence?): FilterResults {
            val result = FilterResults()
            result.values = items
            result.count = items.size
            return result
        }

        protected override fun publishResults(arg0: CharSequence?, arg1: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    init {
        Log.v("Krzys", "Adapter created $filter")
        items = objects
    }
}