package com.serebit.strife.data

/**
 * A [MutableList] implementation which has a maximum size and will throw an exception when adding on max size.
 *
 * @property maxSize The upper bound of the list (maximum size)
 */
class BoundedList<E>(val maxSize: Int) : MutableList<E> {
    private val backingList: MutableList<E> = mutableListOf()
    override val size get() = backingList.size

    /** Add the [element] to the end of the list. Throws an [IllegalStateException] if the list is at max size. */
    override fun add(element: E): Boolean {
        check(size < maxSize) { "Cannot add to BoundedList at max size ($maxSize)" }
        return backingList.add(element)
    }

    /**
     * Inserts an element into the list at the specified [index].
     * Throws an [IllegalStateException] if the list is at max size.
     */
    override fun add(index: Int, element: E) {
        check(size < maxSize) { "Cannot add to BoundedList at max size ($maxSize)" }
        return backingList.add(index, element)
    }

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     * The elements are appended in the order they appear in the [elements] collection.
     *
     * Throws an [IllegalStateException] if the list is at max size.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(elements: Collection<E>): Boolean {
        check(size + elements.size <= maxSize) { "BoundedList would exceed max size ($maxSize)" }
        return backingList.addAll(elements)
    }

    /**
     * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
     * Throws an [IllegalStateException] if the list is at max size.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        check(size + elements.size <= maxSize) { "BoundedList would exceed max size ($maxSize)" }
        return backingList.addAll(index, elements)
    }

    override fun set(index: Int, element: E): E {
        require(index in 0 until maxSize) { "$index is out of the bounded range ${0 until maxSize}" }
        return backingList.set(index, element)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        require(fromIndex in 0 until maxSize && toIndex in fromIndex until maxSize) {
            "Invalid range ${fromIndex until toIndex} for BoundedList of maxSize=$maxSize"
        }
        return backingList.subList(fromIndex, toIndex)
    }

    override fun contains(element: E): Boolean = element in backingList

    override fun containsAll(elements: Collection<E>): Boolean = backingList.containsAll(elements)

    override fun get(index: Int): E = backingList[index]

    override fun indexOf(element: E): Int = backingList.indexOf(element)

    override fun isEmpty(): Boolean = backingList.isEmpty()

    override fun iterator(): MutableIterator<E> = backingList.iterator()

    override fun lastIndexOf(element: E): Int = backingList.lastIndexOf(element)

    override fun clear(): Unit = backingList.clear()

    override fun listIterator(): MutableListIterator<E> = backingList.listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = backingList.listIterator(index)

    override fun remove(element: E): Boolean = backingList.remove(element)

    override fun removeAll(elements: Collection<E>): Boolean = backingList.removeAll(elements)

    override fun removeAt(index: Int): E = backingList.removeAt(index)

    override fun retainAll(elements: Collection<E>): Boolean = backingList.retainAll(elements)
}

/** Creates a [BoundedList] with the given [maxSize] and [elements]. */
fun <E> boundedListOf(maxSize: Int, vararg elements: E): BoundedList<E> {
    require(maxSize >= 0) { "maxSize must be a positive integer." }
    require(elements.size <= maxSize) { "Too many elements (maxSize=$maxSize, elementCount=${elements.size})" }
    return BoundedList<E>(maxSize).apply { addAll(elements) }
}
