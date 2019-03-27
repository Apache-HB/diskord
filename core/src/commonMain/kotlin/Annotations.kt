package com.serebit.strife

/** Marks anything which a bot client cannot access (GroupDM,..). */
annotation class BotInaccessible(val name: String = "", val why: String = "")

/** Marks an element which should never be Empty. */
annotation class NeverEmpty
