package ca.allanwang.gitdroid.data.helpers

import github.fragment.TreeEntryItem

object GitComparators {
    /**
     * Comparator where we display folders first
     */
    fun treeEntryItem() = Comparator<TreeEntryItem> { o1, o2 ->
        when {
            o1 === o2 -> 0
            o1 == null -> 1
            o2 == null -> -1
            o1.obj is TreeEntryItem.AsBlob == o2.obj is TreeEntryItem.AsBlob -> o1.name.compareTo(o2.name)
            o1.obj is TreeEntryItem.AsBlob -> 1
            else -> -1
        }
    }
}