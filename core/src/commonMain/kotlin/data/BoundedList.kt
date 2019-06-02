package com.serebit.strife.data

/**
 * A [MutableList] implementation which has a maximum size and will throw an exception when adding on max size.
 *
 * @property maxSize The upper bound of the list (maximum size)
 */
class BoundedList<E>(val maxSize: Int) : MutableList<E> {
    private val backingList = mutableListOf<E>()
    /** Returns the size of the list. */
    override val size: Int get() = backingList.size

    /** Add the [element] to the end of the list. Throws an [IllegalStateException] if the list is at max size. */
    override fun add(element: E): Boolean {
        check(size < maxSize) { "Cannot add to BoundedList at max size ($maxSize)" }
        return backingList.add(element)
    }

    /**
     * Inserts an [element] into the list at the specified [index].
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

    /** Replaces the element at the specified [index] in the list with the specified [element]. */
    override fun set(index: Int, element: E): E {
        require(index in 0 until maxSize) { "$index is out of the bounded range ${0 until maxSize}" }
        return backingList.set(index, element)
    }

    /**
     * Returns a view of the portion of this list between the specified [fromIndex] (inclusive) and [toIndex]
     * (exclusive). The returned list is backed by this list, so non-structural changes in the returned list are
     * reflected in this list, and vice-versa.
     *
     * Structural changes in the base list make the behavior of the view undefined.
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        require(fromIndex in 0 until maxSize && toIndex in fromIndex until maxSize) {
            "Invalid range ${fromIndex until toIndex} for BoundedList of maxSize=$maxSize"
        }
        return backingList.subList(fromIndex, toIndex)
    }

    /** Checks if the specified element is contained in this collection. */
    override fun contains(element: E): Boolean = element in backingList

    /** Checks if all elements in the specified collection are contained in this collection. */
    override fun containsAll(elements: Collection<E>): Boolean = backingList.containsAll(elements)

    /** Returns the element at the specified index in the list. */
    override fun get(index: Int): E = backingList[index]

    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    override fun indexOf(element: E): Int = backingList.indexOf(element)

    /** Returns `true` if the collection is empty (contains no elements), `false` otherwise. */
    override fun isEmpty(): Boolean = backingList.isEmpty()

    /** Returns an iterator over the elements of this object. */
    override fun iterator(): MutableIterator<E> = backingList.iterator()

    /**
     * Returns the index of the last occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    override fun lastIndexOf(element: E): Int = backingList.lastIndexOf(element)

    /** Removes all elements from this collection. */
    override fun clear(): Unit = backingList.clear()

    /** Returns a list iterator over the elements in this list (in proper sequence). */
    override fun listIterator(): MutableListIterator<E> = backingList.listIterator()

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified [index].
     */
    override fun listIterator(index: Int): MutableListIterator<E> = backingList.listIterator(index)

    /**
     * Removes a single instance of the specified element from this collection, if it is present.
     *
     * @return `true` if the element has been successfully removed; `false` if it was not present in the collection.
     */
    override fun remove(element: E): Boolean = backingList.remove(element)

    /**
     * Removes all of this collection's elements that are also contained in the specified collection.
     *
     * @return `true` if any of the specified elements was removed from the collection, `false` if the collection was
     * not modified.
     */
    override fun removeAll(elements: Collection<E>): Boolean = backingList.removeAll(elements)

    /**
     * Removes an element at the specified [index] from the list.
     *
     * @return the element that has been removed.
     */
    override fun removeAt(index: Int): E = backingList.removeAt(index)

    /**
     * Retains only the elements in this collection that are contained in the specified collection.
     *
     * @return `true` if any element was removed from the collection, `false` if the collection was not modified.
     */
    override fun retainAll(elements: Collection<E>): Boolean = backingList.retainAll(elements)
}

/** Creates a [BoundedList] with the given [maxSize] and [elements]. */
fun <E> boundedListOf(maxSize: Int, vararg elements: E): BoundedList<E> {
    require(maxSize >= 0) { "maxSize must be a positive integer." }
    require(elements.size <= maxSize) { "Too many elements (maxSize=$maxSize, elementCount=${elements.size})" }
    return BoundedList<E>(maxSize).apply { addAll(elements) }
}
