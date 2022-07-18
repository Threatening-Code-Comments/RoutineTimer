package de.threateningcodecomments.accessibility

import java.util.function.Predicate
import java.util.function.UnaryOperator

class ObservableList<T>(list: ArrayList<T> = ArrayList()) : ArrayList<T>(list) {
    fun doOnUpdate(block: UpdateRunnable) {
        update = block
    }

    private var update: UpdateRunnable = { _, _, _ -> }

    private var bufferedList = this

    private fun update(operation: UROperation, element: Any?) {
        val oldList = bufferedList

        update(oldList, operation, element)

        bufferedList = this
    }

    override fun add(element: T): Boolean {
        val add = super.add(element)
        update(UROperation.ADD, element)
        return add
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        update(UROperation.ADD, element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val addAll = super.addAll(elements)
        update(UROperation.ADD_ALL, elements)
        return addAll
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val addAll = super.addAll(index, elements)
        update(UROperation.ADD_ALL, elements)
        return addAll
    }

    override fun clear() {
        super.clear()
        update(UROperation.CLEAR, null)
    }

    override fun remove(element: T): Boolean {
        val remove = super.remove(element)
        update(UROperation.REMOVE, element)
        return remove
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val removeAll = super.removeAll(elements)
        update(UROperation.REMOVE_ALL, elements)
        return removeAll
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        val removeIf = super.removeIf(filter)
        update(UROperation.REMOVE_IF, filter)
        return removeIf
    }

    override fun removeAt(index: Int): T {
        val removeAt = super.removeAt(index)
        update(UROperation.REMOVE_AT, index)
        return removeAt
    }

    override fun set(index: Int, element: T): T {
        val set = super.set(index, element)
        update(UROperation.SET, Pair(index, element))
        return set
    }

    override fun get(index: Int): T {
        val get = super.get(index)
        update(UROperation.GET, index)
        return get
    }

    override fun replaceAll(operator: UnaryOperator<T>) {
        super.replaceAll(operator)
        update(UROperation.REPLACE_ALL, operator)
    }
}