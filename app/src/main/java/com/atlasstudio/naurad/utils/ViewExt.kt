package com.atlasstudio.naurad.utils

import androidx.appcompat.widget.SearchView

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            // TBD
            return true
        }
    })
}

inline fun SearchView.OnSuggestionListener(crossinline listener: (String) -> Unit) {
    this.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
        override fun onSuggestionClick(position: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSuggestionSelect(position: Int): Boolean {
            TODO("Not yet implemented")
        }
    })
}