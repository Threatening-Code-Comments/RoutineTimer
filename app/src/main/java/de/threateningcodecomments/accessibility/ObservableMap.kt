package de.threateningcodecomments.accessibility

import java.util.function.BiFunction

/**
 * Runnable that gets called on an update to an [ObservableMap] or [ObservableList].
 *
 * @param[listBefore] copy of list before operation.
 * @param[operation] type of operation. For list of possible operations. For a list, see [UROperation].
 * @param[element] element used for the operation. If something was added, this is it. If something was removed, this
 * is the place, etc.
 */
typealias UpdateRunnable = (listBefore: Any, operation: UROperation, element: Any?) -> Unit

enum class UROperation {
    ADD,

    /**
     * Element is a Pair(index, element)
     */
    SET,

    /**
     * Element is element to remove
     */
    REMOVE,

    /**
     * Element is Index
     */
    REMOVE_AT,
    PUT,
    CLEAR,
    ADD_ALL,
    REMOVE_ALL,
    REMOVE_IF,
    PUT_ALL,

    /**
     * Element is Triple(key, oldValue: Any?, newValue)
     */
    REPLACE,
    REPLACE_ALL,
    GET;

    companion object {
        /**
         * Includes [ADD], [PUT], [ADD_ALL] and [PUT_ALL]
         */
        val ADDERS = setOf(ADD, PUT, ADD_ALL, PUT_ALL)

        /**
         * Includes [REMOVE], [REMOVE_AT], [REMOVE_ALL], [CLEAR]
         */
        val DIMINISHERS = setOf(REMOVE, REMOVE_AT, REMOVE_ALL, CLEAR)

        /**
         * Includes [SET], [REPLACE] and [REPLACE_ALL]
         */
        val MODIFIERS = setOf(SET, REPLACE, REPLACE_ALL)
    }
}

class ObservableMap<K, V> : HashMap<K, V>() {
    fun doOnUpdate(block: UpdateRunnable) {
        update = block
    }

    private var update: UpdateRunnable = { _, _, _ -> }

    override fun clear() {
        val listBefore = this
        super.clear()
        update(listBefore, UROperation.CLEAR, null)
    }

    override fun put(key: K, value: V): V? {
        val listBefore = this
        val put = super.put(key, value)
        update(listBefore, UROperation.PUT, Pair(key, value))
        return put
    }

    override fun putAll(from: Map<out K, V>) {
        val listBefore = this
        super.putAll(from)
        update(listBefore, UROperation.PUT_ALL, from)
    }

    override fun remove(key: K): V? {
        val listBefore = this
        val remove = super.remove(key)
        update(listBefore, UROperation.REMOVE, key)
        return remove
    }

    override fun remove(key: K, value: V): Boolean {
        val listBefore = this
        val remove = super.remove(key, value)
        update(listBefore, UROperation.REMOVE, key)
        return remove
    }

    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        val listBefore = this
        val replace = super.replace(key, oldValue, newValue)
        update(listBefore, UROperation.REPLACE, Triple(key, oldValue, newValue))
        return replace
    }

    override fun replace(key: K, value: V): V? {
        val listBefore = this
        val replace = super.replace(key, value)
        update(listBefore, UROperation.REPLACE, Triple(key, null, value))
        return replace
    }

    override fun replaceAll(function: BiFunction<in K, in V, out V>) {
        val listBefore = this
        super.replaceAll(function)
        update(listBefore, UROperation.REPLACE_ALL, function)
    }
}