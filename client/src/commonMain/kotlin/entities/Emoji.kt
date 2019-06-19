package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.internal.entitydata.GuildEmojiData
import com.serebit.strife.internal.network.Cdn
import com.serebit.strife.internal.network.ImageFormat
import com.serebit.strife.internal.packets.PartialEmojiPacket
import io.ktor.http.encodeURLQueryComponent

/** A small digital image or icon used to express an idea emotion, etc. */
sealed class Emoji {
    /** How the emoji should be presented in the request body. */
    internal abstract val requestData: String
    /** How the emoji should be presented in the URI. */
    internal abstract val uriData: String
}

/** A user-created Emoji which is housed in a [Guild]. */
class GuildEmoji internal constructor(
    private val data: GuildEmojiData
) : Emoji(), Entity {

    override val requestData = data.id.toString()
    override val uriData = "${data.name}:${data.id}"

    override val id: Long = data.id
    override val context: BotClient = data.context
    /** The name of the [GuildEmoji]. This is used to type the emoji between semi-colons like `:this:` */
    val name: String get() = data.name
    /** The [User] who created this [GuildEmoji]. */
    val creator: User? get() = data.creator?.lazyEntity
    /** The guild roles that are allowed to use this emoji. I think. Discord docs aren't very specific. */
    val whitelistedRoles: List<GuildRole> get() = data.roles.map { it.lazyEntity }
    /** Whether or not this emoji is an animated GIF. */
    val isAnimated: Boolean = data.isAnimated
    /** Whether or not this emoji is managed by an external service, such as a Discord bot. */
    val isManaged: Boolean get() = data.isManaged

    /** Get this [GuildEmoji] as a standard Mention (`<name:ID>`). */
    val asMention: String get() = "<${if (isAnimated) "a:" else ""}$name:$id>"

    /** The URL which leads to the full-sized image. */
    val url: String = Cdn.CustomEmoji(id, if (isAnimated) ImageFormat.Gif else ImageFormat.Png).toString()

    /** Get this [GuildEmoji] as a standard Mention (``<name:ID>``). */
    override fun toString() = asMention
}

/**
 * A user-created [Emoji] which the current client cannot use (i.e. the bot is not a member of the [ForeignGuildEmoji]'s
 * housing [Guild]).
 */
data class ForeignGuildEmoji internal constructor(
    override val context: BotClient,
    override val id: Long,
    /** The name by which this emoji can be mentioned. */
    val name: String
) : Emoji(), Entity {
    override val requestData = id.toString()
    override val uriData = "name:$id"

    /** Get this [GuildEmoji] as a standard Mention (``<name:ID>``). */
    val asMention: String = "<:$name:$id>"

    /** Get this [GuildEmoji] as a standard Mention (``<name:ID>``). */
    override fun toString(): String = asMention
}

/**
 * A Standard Unicode [Emoji] represented in [unicode] characters. If the emoji supports a [SkinTone], [tone] property
 * can be set.
 *
 * @property unicode The unicode character(s) corresponding to this emoji.
 * @property tone The skin tone corresponding to this emoji, or null if it has no skin tone.
 */
sealed class UnicodeEmoji(val unicode: String, val tone: SkinTone? = null) : Emoji() {
    /** The emoji's [unicode] combined with its [tone]'s unicode, if any. */
    val combinedUnicode: String = tone?.let { unicode + it.unicode } ?: unicode

    override val requestData = combinedUnicode
    override val uriData = combinedUnicode.encodeURLQueryComponent()

    // People

    /** Unicode emoji with a Discord shortcode of `:grinning:`. Represented in Unicode as 😀. */
    object Grinning : UnicodeEmoji("\ud83d\ude00")

    /** Unicode emoji with a Discord shortcode of `:grimacing:`. Represented in Unicode as 😬. */
    object Grimacing : UnicodeEmoji("\ud83d\ude2c")

    /** Unicode emoji with a Discord shortcode of `:grin:`. Represented in Unicode as 😁. */
    object Grin : UnicodeEmoji("\ud83d\ude01")

    /** Unicode emoji with a Discord shortcode of `:joy:`. Represented in Unicode as 😂. */
    object Joy : UnicodeEmoji("\ud83d\ude02")

    /** Unicode emoji with a Discord shortcode of `:smiley:`. Represented in Unicode as 😃. */
    object Smiley : UnicodeEmoji("\ud83d\ude03")

    /** Unicode emoji with a Discord shortcode of `:smile:`. Represented in Unicode as 😄. */
    object Smile : UnicodeEmoji("\ud83d\ude04")

    /** Unicode emoji with a Discord shortcode of `:sweat_smile:`. Represented in Unicode as 😅. */
    object SweatSmile : UnicodeEmoji("\ud83d\ude05")

    /** Unicode emoji with a Discord shortcode of `:laughing:`. Represented in Unicode as 😆. */
    object Laughing : UnicodeEmoji("\ud83d\ude06")

    /** Unicode emoji with a Discord shortcode of `:innocent:`. Represented in Unicode as 😇. */
    object Innocent : UnicodeEmoji("\ud83d\ude07")

    /** Unicode emoji with a Discord shortcode of `:wink:`. Represented in Unicode as 😉. */
    object Wink : UnicodeEmoji("\ud83d\ude09")

    /** Unicode emoji with a Discord shortcode of `:blush:`. Represented in Unicode as 😊. */
    object Blush : UnicodeEmoji("\ud83d\ude0a")

    /** Unicode emoji with a Discord shortcode of `:slight_smile:`. Represented in Unicode as 🙂. */
    object SlightSmile : UnicodeEmoji("\ud83d\ude42")

    /** Unicode emoji with a Discord shortcode of `:upside_down:`. Represented in Unicode as 🙃. */
    object UpsideDown : UnicodeEmoji("\ud83d\ude43")

    /** Unicode emoji with a Discord shortcode of `:relaxed:`. Represented in Unicode as ☺. */
    object Relaxed : UnicodeEmoji("\u263a")

    /** Unicode emoji with a Discord shortcode of `:yum:`. Represented in Unicode as 😋. */
    object Yum : UnicodeEmoji("\ud83d\ude0b")

    /** Unicode emoji with a Discord shortcode of `:relieved:`. Represented in Unicode as 😌. */
    object Relieved : UnicodeEmoji("\ud83d\ude0c")

    /** Unicode emoji with a Discord shortcode of `:heart_eyes:`. Represented in Unicode as 😍. */
    object HeartEyes : UnicodeEmoji("\ud83d\ude0d")

    /** Unicode emoji with a Discord shortcode of `:kissing_heart:`. Represented in Unicode as 😘. */
    object KissingHeart : UnicodeEmoji("\ud83d\ude18")

    /** Unicode emoji with a Discord shortcode of `:kissing:`. Represented in Unicode as 😗. */
    object Kissing : UnicodeEmoji("\ud83d\ude17")

    /** Unicode emoji with a Discord shortcode of `:kissing_smiling_eyes:`. Represented in Unicode as 😙. */
    object KissingSmilingEyes : UnicodeEmoji("\ud83d\ude19")

    /** Unicode emoji with a Discord shortcode of `:kissing_closed_eyes:`. Represented in Unicode as 😚. */
    object KissingClosedEyes : UnicodeEmoji("\ud83d\ude1a")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue_winking_eye:`. Represented in Unicode as 😜. */
    object StuckOutTongueWinkingEye : UnicodeEmoji("\ud83d\ude1c")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue_closed_eyes:`. Represented in Unicode as 😝. */
    object StuckOutTongueClosedEyes : UnicodeEmoji("\ud83d\ude1d")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue:`. Represented in Unicode as 😛. */
    object StuckOutTongue : UnicodeEmoji("\ud83d\ude1b")

    /** Unicode emoji with a Discord shortcode of `:money_mouth:`. Represented in Unicode as 🤑. */
    object MoneyMouth : UnicodeEmoji("\ud83e\udd11")

    /** Unicode emoji with a Discord shortcode of `:nerd:`. Represented in Unicode as 🤓. */
    object Nerd : UnicodeEmoji("\ud83e\udd13")

    /** Unicode emoji with a Discord shortcode of `:sunglasses:`. Represented in Unicode as 😎. */
    object Sunglasses : UnicodeEmoji("\ud83d\ude0e")

    /** Unicode emoji with a Discord shortcode of `:hugging:`. Represented in Unicode as 🤗. */
    object Hugging : UnicodeEmoji("\ud83e\udd17")

    /** Unicode emoji with a Discord shortcode of `:smirk:`. Represented in Unicode as 😏. */
    object Smirk : UnicodeEmoji("\ud83d\ude0f")

    /** Unicode emoji with a Discord shortcode of `:no_mouth:`. Represented in Unicode as 😶. */
    object NoMouth : UnicodeEmoji("\ud83d\ude36")

    /** Unicode emoji with a Discord shortcode of `:neutral_face:`. Represented in Unicode as 😐. */
    object NeutralFace : UnicodeEmoji("\ud83d\ude10")

    /** Unicode emoji with a Discord shortcode of `:expressionless:`. Represented in Unicode as 😑. */
    object Expressionless : UnicodeEmoji("\ud83d\ude11")

    /** Unicode emoji with a Discord shortcode of `:unamused:`. Represented in Unicode as 😒. */
    object Unamused : UnicodeEmoji("\ud83d\ude12")

    /** Unicode emoji with a Discord shortcode of `:rolling_eyes:`. Represented in Unicode as 🙄. */
    object RollingEyes : UnicodeEmoji("\ud83d\ude44")

    /** Unicode emoji with a Discord shortcode of `:thinking:`. Represented in Unicode as 🤔. */
    object Thinking : UnicodeEmoji("\ud83e\udd14")

    /** Unicode emoji with a Discord shortcode of `:flushed:`. Represented in Unicode as 😳. */
    object Flushed : UnicodeEmoji("\ud83d\ude33")

    /** Unicode emoji with a Discord shortcode of `:disappointed:`. Represented in Unicode as 😞. */
    object Disappointed : UnicodeEmoji("\ud83d\ude1e")

    /** Unicode emoji with a Discord shortcode of `:worried:`. Represented in Unicode as 😟. */
    object Worried : UnicodeEmoji("\ud83d\ude1f")

    /** Unicode emoji with a Discord shortcode of `:angry:`. Represented in Unicode as 😠. */
    object Angry : UnicodeEmoji("\ud83d\ude20")

    /** Unicode emoji with a Discord shortcode of `:rage:`. Represented in Unicode as 😡. */
    object Rage : UnicodeEmoji("\ud83d\ude21")

    /** Unicode emoji with a Discord shortcode of `:pensive:`. Represented in Unicode as 😔. */
    object Pensive : UnicodeEmoji("\ud83d\ude14")

    /** Unicode emoji with a Discord shortcode of `:confused:`. Represented in Unicode as 😕. */
    object Confused : UnicodeEmoji("\ud83d\ude15")

    /** Unicode emoji with a Discord shortcode of `:slight_frown:`. Represented in Unicode as 🙁. */
    object SlightFrown : UnicodeEmoji("\ud83d\ude41")

    /** Unicode emoji with a Discord shortcode of `:frowning2:`. Represented in Unicode as ☹. */
    object Frowning2 : UnicodeEmoji("\u2639")

    /** Unicode emoji with a Discord shortcode of `:persevere:`. Represented in Unicode as 😣. */
    object Persevere : UnicodeEmoji("\ud83d\ude23")

    /** Unicode emoji with a Discord shortcode of `:confounded:`. Represented in Unicode as 😖. */
    object Confounded : UnicodeEmoji("\ud83d\ude16")

    /** Unicode emoji with a Discord shortcode of `:tired_face:`. Represented in Unicode as 😫. */
    object TiredFace : UnicodeEmoji("\ud83d\ude2b")

    /** Unicode emoji with a Discord shortcode of `:weary:`. Represented in Unicode as 😩. */
    object Weary : UnicodeEmoji("\ud83d\ude29")

    /** Unicode emoji with a Discord shortcode of `:triumph:`. Represented in Unicode as 😤. */
    object Triumph : UnicodeEmoji("\ud83d\ude24")

    /** Unicode emoji with a Discord shortcode of `:open_mouth:`. Represented in Unicode as 😮. */
    object OpenMouth : UnicodeEmoji("\ud83d\ude2e")

    /** Unicode emoji with a Discord shortcode of `:scream:`. Represented in Unicode as 😱. */
    object Scream : UnicodeEmoji("\ud83d\ude31")

    /** Unicode emoji with a Discord shortcode of `:fearful:`. Represented in Unicode as 😨. */
    object Fearful : UnicodeEmoji("\ud83d\ude28")

    /** Unicode emoji with a Discord shortcode of `:cold_sweat:`. Represented in Unicode as 😰. */
    object ColdSweat : UnicodeEmoji("\ud83d\ude30")

    /** Unicode emoji with a Discord shortcode of `:hushed:`. Represented in Unicode as 😯. */
    object Hushed : UnicodeEmoji("\ud83d\ude2f")

    /** Unicode emoji with a Discord shortcode of `:frowning:`. Represented in Unicode as 😦. */
    object Frowning : UnicodeEmoji("\ud83d\ude26")

    /** Unicode emoji with a Discord shortcode of `:anguished:`. Represented in Unicode as 😧. */
    object Anguished : UnicodeEmoji("\ud83d\ude27")

    /** Unicode emoji with a Discord shortcode of `:cry:`. Represented in Unicode as 😢. */
    object Cry : UnicodeEmoji("\ud83d\ude22")

    /** Unicode emoji with a Discord shortcode of `:disappointed_relieved:`. Represented in Unicode as 😥. */
    object DisappointedRelieved : UnicodeEmoji("\ud83d\ude25")

    /** Unicode emoji with a Discord shortcode of `:sleepy:`. Represented in Unicode as 😪. */
    object Sleepy : UnicodeEmoji("\ud83d\ude2a")

    /** Unicode emoji with a Discord shortcode of `:sweat:`. Represented in Unicode as 😓. */
    object Sweat : UnicodeEmoji("\ud83d\ude13")

    /** Unicode emoji with a Discord shortcode of `:sob:`. Represented in Unicode as 😭. */
    object Sob : UnicodeEmoji("\ud83d\ude2d")

    /** Unicode emoji with a Discord shortcode of `:dizzy_face:`. Represented in Unicode as 😵. */
    object DizzyFace : UnicodeEmoji("\ud83d\ude35")

    /** Unicode emoji with a Discord shortcode of `:astonished:`. Represented in Unicode as 😲. */
    object Astonished : UnicodeEmoji("\ud83d\ude32")

    /** Unicode emoji with a Discord shortcode of `:zipper_mouth:`. Represented in Unicode as 🤐. */
    object ZipperMouth : UnicodeEmoji("\ud83e\udd10")

    /** Unicode emoji with a Discord shortcode of `:mask:`. Represented in Unicode as 😷. */
    object Mask : UnicodeEmoji("\ud83d\ude37")

    /** Unicode emoji with a Discord shortcode of `:thermometer_face:`. Represented in Unicode as 🤒. */
    object ThermometerFace : UnicodeEmoji("\ud83e\udd12")

    /** Unicode emoji with a Discord shortcode of `:head_bandage:`. Represented in Unicode as 🤕. */
    object HeadBandage : UnicodeEmoji("\ud83e\udd15")

    /** Unicode emoji with a Discord shortcode of `:sleeping:`. Represented in Unicode as 😴. */
    object Sleeping : UnicodeEmoji("\ud83d\ude34")

    /** Unicode emoji with a Discord shortcode of `:zzz:`. Represented in Unicode as 💤. */
    object Zzz : UnicodeEmoji("\ud83d\udca4")

    /** Unicode emoji with a Discord shortcode of `:poop:`. Represented in Unicode as 💩. */
    object Poop : UnicodeEmoji("\ud83d\udca9")

    /** Unicode emoji with a Discord shortcode of `:smiling_imp:`. Represented in Unicode as 😈. */
    object SmilingImp : UnicodeEmoji("\ud83d\ude08")

    /** Unicode emoji with a Discord shortcode of `:imp:`. Represented in Unicode as 👿. */
    object Imp : UnicodeEmoji("\ud83d\udc7f")

    /** Unicode emoji with a Discord shortcode of `:japanese_ogre:`. Represented in Unicode as 👹. */
    object JapaneseOgre : UnicodeEmoji("\ud83d\udc79")

    /** Unicode emoji with a Discord shortcode of `:japanese_goblin:`. Represented in Unicode as 👺. */
    object JapaneseGoblin : UnicodeEmoji("\ud83d\udc7a")

    /** Unicode emoji with a Discord shortcode of `:skull:`. Represented in Unicode as 💀. */
    object Skull : UnicodeEmoji("\ud83d\udc80")

    /** Unicode emoji with a Discord shortcode of `:ghost:`. Represented in Unicode as 👻. */
    object Ghost : UnicodeEmoji("\ud83d\udc7b")

    /** Unicode emoji with a Discord shortcode of `:alien:`. Represented in Unicode as 👽. */
    object Alien : UnicodeEmoji("\ud83d\udc7d")

    /** Unicode emoji with a Discord shortcode of `:robot:`. Represented in Unicode as 🤖. */
    object Robot : UnicodeEmoji("\ud83e\udd16")

    /** Unicode emoji with a Discord shortcode of `:smiley_cat:`. Represented in Unicode as 😺. */
    object SmileyCat : UnicodeEmoji("\ud83d\ude3a")

    /** Unicode emoji with a Discord shortcode of `:smile_cat:`. Represented in Unicode as 😸. */
    object SmileCat : UnicodeEmoji("\ud83d\ude38")

    /** Unicode emoji with a Discord shortcode of `:joy_cat:`. Represented in Unicode as 😹. */
    object JoyCat : UnicodeEmoji("\ud83d\ude39")

    /** Unicode emoji with a Discord shortcode of `:heart_eyes_cat:`. Represented in Unicode as 😻. */
    object HeartEyesCat : UnicodeEmoji("\ud83d\ude3b")

    /** Unicode emoji with a Discord shortcode of `:smirk_cat:`. Represented in Unicode as 😼. */
    object SmirkCat : UnicodeEmoji("\ud83d\ude3c")

    /** Unicode emoji with a Discord shortcode of `:kissing_cat:`. Represented in Unicode as 😽. */
    object KissingCat : UnicodeEmoji("\ud83d\ude3d")

    /** Unicode emoji with a Discord shortcode of `:scream_cat:`. Represented in Unicode as 🙀. */
    object ScreamCat : UnicodeEmoji("\ud83d\ude40")

    /** Unicode emoji with a Discord shortcode of `:crying_cat_face:`. Represented in Unicode as 😿. */
    object CryingCatFace : UnicodeEmoji("\ud83d\ude3f")

    /** Unicode emoji with a Discord shortcode of `:pouting_cat:`. Represented in Unicode as 😾. */
    object PoutingCat : UnicodeEmoji("\ud83d\ude3e")

    /** Unicode emoji with a Discord shortcode of `:raised_hands:` and the given skin [tone]. Represented in Unicode as 🙌. */
    class RaisedHands(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4c", tone)

    /** Unicode emoji with a Discord shortcode of `:clap:` and the given skin [tone]. Represented in Unicode as 👏. */
    class Clap(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4f", tone)

    /** Unicode emoji with a Discord shortcode of `:wave:` and the given skin [tone]. Represented in Unicode as 👋. */
    class Wave(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4b", tone)

    /** Unicode emoji with a Discord shortcode of `:thumbsup:` and the given skin [tone]. Represented in Unicode as 👍. */
    class Thumbsup(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4d", tone)

    /** Unicode emoji with a Discord shortcode of `:thumbsdown:` and the given skin [tone]. Represented in Unicode as 👎. */
    class Thumbsdown(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4e", tone)

    /** Unicode emoji with a Discord shortcode of `:punch:` and the given skin [tone]. Represented in Unicode as 👊. */
    class Punch(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4a", tone)

    /** Unicode emoji with a Discord shortcode of `:fist:` and the given skin [tone]. Represented in Unicode as ✊. */
    class Fist(tone: SkinTone? = null) : UnicodeEmoji("\u270a", tone)

    /** Unicode emoji with a Discord shortcode of `:v:` and the given skin [tone]. Represented in Unicode as 👌. */
    class V(tone: SkinTone? = null) : UnicodeEmoji("\u270c", tone)

    /** Unicode emoji with a Discord shortcode of `:ok_hand:` and the given skin [tone]. Represented in Unicode as 👌. */
    class OkHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4c", tone)

    /** Unicode emoji with a Discord shortcode of `:raised_hand:` and the given skin [tone]. Represented in Unicode as ✋. */
    class RaisedHand(tone: SkinTone? = null) : UnicodeEmoji("\u270b", tone)

    /** Unicode emoji with a Discord shortcode of `:open_hands:` and the given skin [tone]. Represented in Unicode as 👐. */
    class OpenHands(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc50", tone)

    /** Unicode emoji with a Discord shortcode of `:muscle:` and the given skin [tone]. Represented in Unicode as 💪. */
    class Muscle(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udcaa", tone)

    /** Unicode emoji with a Discord shortcode of `:pray:` and the given skin [tone]. Represented in Unicode as 🙏. */
    class Pray(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4f", tone)

    /** Unicode emoji with a Discord shortcode of `:point_up:` and the given skin [tone]. Represented in Unicode as ☝. */
    class PointUp(tone: SkinTone? = null) : UnicodeEmoji("\u261d", tone)

    /** Unicode emoji with a Discord shortcode of `:point_up_2:` and the given skin [tone]. Represented in Unicode as 👆. */
    class PointUp2(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc46", tone)

    /** Unicode emoji with a Discord shortcode of `:point_down:` and the given skin [tone]. Represented in Unicode as 👇. */
    class PointDown(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc47", tone)

    /** Unicode emoji with a Discord shortcode of `:point_left:` and the given skin [tone]. Represented in Unicode as 👈. */
    class PointLeft(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc48", tone)

    /** Unicode emoji with a Discord shortcode of `:point_right:` and the given skin [tone]. Represented in Unicode as 👉. */
    class PointRight(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc49", tone)

    /** Unicode emoji with a Discord shortcode of `:middle_finger:` and the given skin [tone]. Represented in Unicode as 🖕. */
    class MiddleFinger(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd95", tone)

    /** Unicode emoji with a Discord shortcode of `:hand_splayed:` and the given skin [tone]. Represented in Unicode as 🖐. */
    class HandSplayed(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd90", tone)

    /** Unicode emoji with a Discord shortcode of `:metal:` and the given skin [tone]. Represented in Unicode as 🤘. */
    class Metal(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd18", tone)

    /** Unicode emoji with a Discord shortcode of `:vulcan:` and the given skin [tone]. Represented in Unicode as 🖖. */
    class Vulcan(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd96", tone)

    /** Unicode emoji with a Discord shortcode of `:writing_hand:` and the given skin [tone]. Represented in Unicode as ✍. */
    class WritingHand(tone: SkinTone? = null) : UnicodeEmoji("\u270d", tone)

    /** Unicode emoji with a Discord shortcode of `:nail_care:` and the given skin [tone]. Represented in Unicode as 💅. */
    class NailCare(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc85", tone)

    /** Unicode emoji with a Discord shortcode of `:lips:`. Represented in Unicode as 👄. */
    object Lips : UnicodeEmoji("\ud83d\udc44")

    /** Unicode emoji with a Discord shortcode of `:tongue:`. Represented in Unicode as 👅. */
    object Tongue : UnicodeEmoji("\ud83d\udc45")

    /** Unicode emoji with a Discord shortcode of `:ear:` and the given skin [tone]. Represented in Unicode as 👂. */
    class Ear(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc42", tone)

    /** Unicode emoji with a Discord shortcode of `:nose:` and the given skin [tone]. Represented in Unicode as 👃. */
    class Nose(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc43", tone)

    /** Unicode emoji with a Discord shortcode of `:eye:`. Represented in Unicode as 👁. */
    object Eye : UnicodeEmoji("\ud83d\udc41")

    /** Unicode emoji with a Discord shortcode of `:eyes:`. Represented in Unicode as 👀. */
    object Eyes : UnicodeEmoji("\ud83d\udc40")

    /** Unicode emoji with a Discord shortcode of `:bust_in_silhouette:`. Represented in Unicode as 👤. */
    object BustInSilhouette : UnicodeEmoji("\ud83d\udc64")

    /** Unicode emoji with a Discord shortcode of `:busts_in_silhouette:`. Represented in Unicode as 👥. */
    object BustsInSilhouette : UnicodeEmoji("\ud83d\udc65")

    /** Unicode emoji with a Discord shortcode of `:speaking_head:`. Represented in Unicode as 🗣. */
    object SpeakingHead : UnicodeEmoji("\ud83d\udde3")

    /** Unicode emoji with a Discord shortcode of `:baby:` and the given skin [tone]. Represented in Unicode as 👶. */
    class Baby(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc76", tone)

    /** Unicode emoji with a Discord shortcode of `:boy:` and the given skin [tone]. Represented in Unicode as 👦. */
    class Boy(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc66", tone)

    /** Unicode emoji with a Discord shortcode of `:girl:` and the given skin [tone]. Represented in Unicode as 👧. */
    class Girl(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc67", tone)

    /** Unicode emoji with a Discord shortcode of `:man:` and the given skin [tone]. Represented in Unicode as 👨. */
    class Man(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68", tone)

    /** Unicode emoji with a Discord shortcode of `:woman:` and the given skin [tone]. Represented in Unicode as 👩. */
    class Woman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69", tone)

    /** Unicode emoji with a Discord shortcode of `:person_with_blond_hair:` and the given skin [tone]. Represented in Unicode as 👱. */
    class PersonWithBlondHair(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc71", tone)

    /** Unicode emoji with a Discord shortcode of `:older_man:` and the given skin [tone]. Represented in Unicode as 👴. */
    class OlderMan(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc74", tone)

    /** Unicode emoji with a Discord shortcode of `:older_woman:` and the given skin [tone]. Represented in Unicode as 👵. */
    class OlderWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc75", tone)

    /** Unicode emoji with a Discord shortcode of `:man_with_gua_pi_mao:` and the given skin [tone]. Represented in Unicode as 👲. */
    class ManWithGuaPiMao(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc72", tone)

    /** Unicode emoji with a Discord shortcode of `:man_with_turban:` and the given skin [tone]. Represented in Unicode as 👳. */
    class ManWithTurban(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc73", tone)

    /** Unicode emoji with a Discord shortcode of `:cop:` and the given skin [tone]. Represented in Unicode as 👮. */
    class Cop(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc6e", tone)

    /** Unicode emoji with a Discord shortcode of `:construction_worker:` and the given skin [tone]. Represented in Unicode as 👷. */
    class ConstructionWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc77", tone)

    /** Unicode emoji with a Discord shortcode of `:guardsman:` and the given skin [tone]. Represented in Unicode as 💂. */
    class Guardsman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc82", tone)

    /** Unicode emoji with a Discord shortcode of `:spy:` and the given skin [tone]. Represented in Unicode as 🕵. */
    class Spy(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd75", tone)

    /** Unicode emoji with a Discord shortcode of `:santa:` and the given skin [tone]. Represented in Unicode as 🎅. */
    class Santa(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udf85", tone)

    /** Unicode emoji with a Discord shortcode of `:angel:` and the given skin [tone]. Represented in Unicode as 👼. */
    class Angel(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc7c", tone)

    /** Unicode emoji with a Discord shortcode of `:princess:` and the given skin [tone]. Represented in Unicode as 👸. */
    class Princess(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc78", tone)

    /** Unicode emoji with a Discord shortcode of `:bride_with_veil:` and the given skin [tone]. Represented in Unicode as 👰. */
    class BrideWithVeil(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc70", tone)

    /** Unicode emoji with a Discord shortcode of `:walking:` and the given skin [tone]. Represented in Unicode as 🚶. */
    class Walking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb6", tone)

    /** Unicode emoji with a Discord shortcode of `:running:` and the given skin [tone]. Represented in Unicode as 🏃. */
    class Running(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc3", tone)

    /** Unicode emoji with a Discord shortcode of `:dancer:` and the given skin [tone]. Represented in Unicode as 💃. */
    class Dancer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc83", tone)

    /** Unicode emoji with a Discord shortcode of `:dancers:`. Represented in Unicode as 👯. */
    object Dancers : UnicodeEmoji("\ud83d\udc6f")

    /** Unicode emoji with a Discord shortcode of `:couple:`. Represented in Unicode as 👫. */
    object Couple : UnicodeEmoji("\ud83d\udc6b")

    /** Unicode emoji with a Discord shortcode of `:two_men_holding_hands:`. Represented in Unicode as 👬. */
    object TwoMenHoldingHands : UnicodeEmoji("\ud83d\udc6c")

    /** Unicode emoji with a Discord shortcode of `:two_women_holding_hands:`. Represented in Unicode as 👭. */
    object TwoWomenHoldingHands : UnicodeEmoji("\ud83d\udc6d")

    /** Unicode emoji with a Discord shortcode of `:bow:` and the given skin [tone]. Represented in Unicode as 🙇. */
    class Bow(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude47", tone)

    /** Unicode emoji with a Discord shortcode of `:information_desk_person:` and the given skin [tone]. Represented in Unicode as 💁. */
    class InformationDeskPerson(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc81", tone)

    /** Unicode emoji with a Discord shortcode of `:no_good:` and the given skin [tone]. Represented in Unicode as 🙅. */
    class NoGood(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude45", tone)

    /** Unicode emoji with a Discord shortcode of `:ok_woman:` and the given skin [tone]. Represented in Unicode as 🙆. */
    class OkWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude46", tone)

    /** Unicode emoji with a Discord shortcode of `:raising_hand:` and the given skin [tone]. Represented in Unicode as 🙋. */
    class RaisingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4b", tone)

    /** Unicode emoji with a Discord shortcode of `:person_with_pouting_face:` and the given skin [tone]. Represented in Unicode as 🙎. */
    class PersonWithPoutingFace(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4e", tone)

    /** Unicode emoji with a Discord shortcode of `:person_frowning:` and the given skin [tone]. Represented in Unicode as 🙍. */
    class PersonFrowning(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4d", tone)

    /** Unicode emoji with a Discord shortcode of `:haircut:` and the given skin [tone]. Represented in Unicode as 💇. */
    class Haircut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc87", tone)

    /** Unicode emoji with a Discord shortcode of `:massage:` and the given skin [tone]. Represented in Unicode as 💆. */
    class Massage(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc86", tone)

    /** Unicode emoji with a Discord shortcode of `:couple_with_heart:`. Represented in Unicode as 💑. */
    object CoupleWithHeart : UnicodeEmoji("\ud83d\udc91")

    /** Unicode emoji with a Discord shortcode of `:couple_ww:`. Represented in Unicode as 👩‍❤️‍👩. */
    object CoupleWW : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc69")

    /** Unicode emoji with a Discord shortcode of `:couple_mm:`. Represented in Unicode as 👨‍❤️‍👨. */
    object CoupleMM : UnicodeEmoji("\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc68")

    /** Unicode emoji with a Discord shortcode of `:couplekiss:`. Represented in Unicode as 💏. */
    object Couplekiss : UnicodeEmoji("\ud83d\udc8f")

    /** Unicode emoji with a Discord shortcode of `:kiss_ww:`. Represented in Unicode as 👩‍❤️‍💋‍👩. */
    object KissWW : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69")

    /** Unicode emoji with a Discord shortcode of `:kiss_mm:`. Represented in Unicode as 👨‍❤️‍💋‍👨. */
    object KissMM : UnicodeEmoji("\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68")

    /** Unicode emoji with a Discord shortcode of `:family:`. Represented in Unicode as 👪. */
    object Family : UnicodeEmoji("\ud83d\udc6a")

    /** Unicode emoji with a Discord shortcode of `:family_mwg:`. Represented in Unicode as 👨‍👩‍👧. */
    object FamilyMwg : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_mwgb:`. Represented in Unicode as 👨‍👩‍👧‍👦. */
    object FamilyMwgb : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_mwbb:`. Represented in Unicode as 👨‍👩‍👦‍👦. */
    object FamilyMwbb : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_mwgg:`. Represented in Unicode as 👨‍👩‍👧‍👧. */
    object FamilyMwgg : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_wwb:`. Represented in Unicode as 👩‍👩‍👦. */
    object FamilyWwb : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_wwg:`. Represented in Unicode as 👩‍👩‍👧. */
    object FamilyWwg : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_wwgb:`. Represented in Unicode as 👩‍👩‍👧‍👦. */
    object FamilyWwgb : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_wwbb:`. Represented in Unicode as 👩‍👩‍👦‍👦. */
    object FamilyWwbb : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_wwgg:`. Represented in Unicode as 👩‍👩‍👧‍👧. */
    object FamilyWwgg : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_mmb:`. Represented in Unicode as 👨‍👨‍👦. */
    object FamilyMmb : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_mmg:`. Represented in Unicode as 👨‍👨‍👧. */
    object FamilyMmg : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_mmgb:`. Represented in Unicode as 👨‍👨‍👧‍👦. */
    object FamilyMmgb : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_mmbb:`. Represented in Unicode as 👨‍👨‍👦‍👦. */
    object FamilyMmbb : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc66\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_mmgg:`. Represented in Unicode as 👨‍👨‍👧‍👧. */
    object FamilyMmgg : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:womans_clothes:`. Represented in Unicode as 👚. */
    object WomansClothes : UnicodeEmoji("\ud83d\udc5a")

    /** Unicode emoji with a Discord shortcode of `:shirt:`. Represented in Unicode as 👕. */
    object Shirt : UnicodeEmoji("\ud83d\udc55")

    /** Unicode emoji with a Discord shortcode of `:jeans:`. Represented in Unicode as 👖. */
    object Jeans : UnicodeEmoji("\ud83d\udc56")

    /** Unicode emoji with a Discord shortcode of `:necktie:`. Represented in Unicode as 👔. */
    object Necktie : UnicodeEmoji("\ud83d\udc54")

    /** Unicode emoji with a Discord shortcode of `:dress:`. Represented in Unicode as 👗. */
    object Dress : UnicodeEmoji("\ud83d\udc57")

    /** Unicode emoji with a Discord shortcode of `:bikini:`. Represented in Unicode as 👙. */
    object Bikini : UnicodeEmoji("\ud83d\udc59")

    /** Unicode emoji with a Discord shortcode of `:kimono:`. Represented in Unicode as 👘. */
    object Kimono : UnicodeEmoji("\ud83d\udc58")

    /** Unicode emoji with a Discord shortcode of `:lipstick:`. Represented in Unicode as 💄. */
    object Lipstick : UnicodeEmoji("\ud83d\udc84")

    /** Unicode emoji with a Discord shortcode of `:kiss:`. Represented in Unicode as 💋. */
    object Kiss : UnicodeEmoji("\ud83d\udc8b")

    /** Unicode emoji with a Discord shortcode of `:footprints:`. Represented in Unicode as 👣. */
    object Footprints : UnicodeEmoji("\ud83d\udc63")

    /** Unicode emoji with a Discord shortcode of `:high_heel:`. Represented in Unicode as 👠. */
    object HighHeel : UnicodeEmoji("\ud83d\udc60")

    /** Unicode emoji with a Discord shortcode of `:sandal:`. Represented in Unicode as 👡. */
    object Sandal : UnicodeEmoji("\ud83d\udc61")

    /** Unicode emoji with a Discord shortcode of `:boot:`. Represented in Unicode as 👢. */
    object Boot : UnicodeEmoji("\ud83d\udc62")

    /** Unicode emoji with a Discord shortcode of `:shoe:`. Represented in Unicode as 👞. */
    object Shoe : UnicodeEmoji("\ud83d\udc5e")

    /** Unicode emoji with a Discord shortcode of `:athletic_shoe:`. Represented in Unicode as 👟. */
    object AthleticShoe : UnicodeEmoji("\ud83d\udc5f")

    /** Unicode emoji with a Discord shortcode of `:womans_hat:`. Represented in Unicode as 👒. */
    object WomansHat : UnicodeEmoji("\ud83d\udc52")

    /** Unicode emoji with a Discord shortcode of `:tophat:`. Represented in Unicode as 🎩. */
    object Tophat : UnicodeEmoji("\ud83c\udfa9")

    /** Unicode emoji with a Discord shortcode of `:helmet_with_cross:`. Represented in Unicode as ⛑. */
    object HelmetWithCross : UnicodeEmoji("\u26d1")

    /** Unicode emoji with a Discord shortcode of `:mortar_board:`. Represented in Unicode as 🎓. */
    object MortarBoard : UnicodeEmoji("\ud83c\udf93")

    /** Unicode emoji with a Discord shortcode of `:crown:`. Represented in Unicode as 👑. */
    object Crown : UnicodeEmoji("\ud83d\udc51")

    /** Unicode emoji with a Discord shortcode of `:school_satchel:`. Represented in Unicode as 🎒. */
    object SchoolSatchel : UnicodeEmoji("\ud83c\udf92")

    /** Unicode emoji with a Discord shortcode of `:pouch:`. Represented in Unicode as 👝. */
    object Pouch : UnicodeEmoji("\ud83d\udc5d")

    /** Unicode emoji with a Discord shortcode of `:purse:`. Represented in Unicode as 👛. */
    object Purse : UnicodeEmoji("\ud83d\udc5b")

    /** Unicode emoji with a Discord shortcode of `:handbag:`. Represented in Unicode as 👜. */
    object Handbag : UnicodeEmoji("\ud83d\udc5c")

    /** Unicode emoji with a Discord shortcode of `:briefcase:`. Represented in Unicode as 💼. */
    object Briefcase : UnicodeEmoji("\ud83d\udcbc")

    /** Unicode emoji with a Discord shortcode of `:eyeglasses:`. Represented in Unicode as 👓. */
    object Eyeglasses : UnicodeEmoji("\ud83d\udc53")

    /** Unicode emoji with a Discord shortcode of `:dark_sunglasses:`. Represented in Unicode as 🕶. */
    object DarkSunglasses : UnicodeEmoji("\ud83d\udd76")

    /** Unicode emoji with a Discord shortcode of `:ring:`. Represented in Unicode as 💍. */
    object Ring : UnicodeEmoji("\ud83d\udc8d")

    /** Unicode emoji with a Discord shortcode of `:closed_umbrella:`. Represented in Unicode as 🌂. */
    object ClosedUmbrella : UnicodeEmoji("\ud83c\udf02")

    /** Unicode emoji with a Discord shortcode of `:cowboy:`. Represented in Unicode as 🤠. */
    object Cowboy : UnicodeEmoji("\ud83e\udd20")

    /** Unicode emoji with a Discord shortcode of `:clown:`. Represented in Unicode as 🤡. */
    object Clown : UnicodeEmoji("\ud83e\udd21")

    /** Unicode emoji with a Discord shortcode of `:sick:`. Represented in Unicode as 🤢. */
    object Sick : UnicodeEmoji("\ud83e\udd22")

    /** Unicode emoji with a Discord shortcode of `:rofl:`. Represented in Unicode as 🤣. */
    object Rofl : UnicodeEmoji("\ud83e\udd23")

    /** Unicode emoji with a Discord shortcode of `:drool:`. Represented in Unicode as 🤤. */
    object Drool : UnicodeEmoji("\ud83e\udd24")

    /** Unicode emoji with a Discord shortcode of `:lying:`. Represented in Unicode as 🤥. */
    object Lying : UnicodeEmoji("\ud83e\udd25")

    /** Unicode emoji with a Discord shortcode of `:sneeze:`. Represented in Unicode as 🤧. */
    object Sneeze : UnicodeEmoji("\ud83e\udd27")

    /** Unicode emoji with a Discord shortcode of `:prince:` and the given skin [tone]. Represented in Unicode as 🤴. */
    class Prince(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd34", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_tuxedo:` and the given skin [tone]. Represented in Unicode as 🤵. */
    class ManInTuxedo(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd35", tone)

    /** Unicode emoji with a Discord shortcode of `:mrs_claus:` and the given skin [tone]. Represented in Unicode as 🤶. */
    class MrsClaus(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd36", tone)

    /** Unicode emoji with a Discord shortcode of `:facepalm:` and the given skin [tone]. Represented in Unicode as 🤦. */
    class Facepalm(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd26", tone)

    /** Unicode emoji with a Discord shortcode of `:shrug:` and the given skin [tone]. Represented in Unicode as 🤷. */
    class Shrug(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd37", tone)

    /** Unicode emoji with a Discord shortcode of `:pregnant_woman:` and the given skin [tone]. Represented in Unicode as 🤰. */
    class PregnantWoman(tone: SkinTone? = null) : UnicodeEmoji("\uD83E\uDD30", tone)

    /** Unicode emoji with a Discord shortcode of `:selfie:` and the given skin [tone]. Represented in Unicode as 🤳. */
    class Selfie(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd33", tone)

    /** Unicode emoji with a Discord shortcode of `:man_dancing:` and the given skin [tone]. Represented in Unicode as 🕺. */
    class ManDancing(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd7a", tone)

    /** Unicode emoji with a Discord shortcode of `:call_me:` and the given skin [tone]. Represented in Unicode as 🤙. */
    class CallMe(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd19", tone)

    /** Unicode emoji with a Discord shortcode of `:raised_back_of_hand:` and the given skin [tone]. Represented in Unicode as 🤚. */
    class RaisedBackOfHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1a", tone)

    /** Unicode emoji with a Discord shortcode of `:left_fist:` and the given skin [tone]. Represented in Unicode as 🤞. */
    class LeftFist(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1e", tone)

    /** Unicode emoji with a Discord shortcode of `:right_fist:` and the given skin [tone]. Represented in Unicode as 🤜. */
    class RightFist(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1c", tone)

    /** Unicode emoji with a Discord shortcode of `:handshake:` and the given skin [tone]. Represented in Unicode as 🤝. */
    class Handshake(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1d", tone)

    /** Unicode emoji with a Discord shortcode of `:fingers_crossed:` and the given skin [tone]. Represented in Unicode as 🤞. */
    class FingersCrossed(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1e", tone)

    // Nature

    /** Unicode emoji with a Discord shortcode of `:dog:`. Represented in Unicode as 🐶. */
    object Dog : UnicodeEmoji("\ud83d\udc36")

    /** Unicode emoji with a Discord shortcode of `:cat:`. Represented in Unicode as 🐱. */
    object Cat : UnicodeEmoji("\ud83d\udc31")

    /** Unicode emoji with a Discord shortcode of `:mouse:`. Represented in Unicode as 🐭. */
    object Mouse : UnicodeEmoji("\ud83d\udc2d")

    /** Unicode emoji with a Discord shortcode of `:hamster:`. Represented in Unicode as 🐹. */
    object Hamster : UnicodeEmoji("\ud83d\udc39")

    /** Unicode emoji with a Discord shortcode of `:rabbit:`. Represented in Unicode as 🐰. */
    object Rabbit : UnicodeEmoji("\ud83d\udc30")

    /** Unicode emoji with a Discord shortcode of `:bear:`. Represented in Unicode as 🐻. */
    object Bear : UnicodeEmoji("\ud83d\udc3b")

    /** Unicode emoji with a Discord shortcode of `:panda_face:`. Represented in Unicode as 🐼. */
    object PandaFace : UnicodeEmoji("\ud83d\udc3c")

    /** Unicode emoji with a Discord shortcode of `:koala:`. Represented in Unicode as 🐨. */
    object Koala : UnicodeEmoji("\ud83d\udc28")

    /** Unicode emoji with a Discord shortcode of `:tiger:`. Represented in Unicode as 🐯. */
    object Tiger : UnicodeEmoji("\ud83d\udc2f")

    /** Unicode emoji with a Discord shortcode of `:lion_face:`. Represented in Unicode as 🦁. */
    object LionFace : UnicodeEmoji("\ud83e\udd81")

    /** Unicode emoji with a Discord shortcode of `:cow:`. Represented in Unicode as 🐮. */
    object Cow : UnicodeEmoji("\ud83d\udc2e")

    /** Unicode emoji with a Discord shortcode of `:pig:`. Represented in Unicode as 🐷. */
    object Pig : UnicodeEmoji("\ud83d\udc37")

    /** Unicode emoji with a Discord shortcode of `:pig_nose:`. Represented in Unicode as 🐽. */
    object PigNose : UnicodeEmoji("\ud83d\udc3d")

    /** Unicode emoji with a Discord shortcode of `:frog:`. Represented in Unicode as 🐸. */
    object Frog : UnicodeEmoji("\ud83d\udc38")

    /** Unicode emoji with a Discord shortcode of `:octopus:`. Represented in Unicode as 🐙. */
    object Octopus : UnicodeEmoji("\ud83d\udc19")

    /** Unicode emoji with a Discord shortcode of `:monkey_face:`. Represented in Unicode as 🐵. */
    object MonkeyFace : UnicodeEmoji("\ud83d\udc35")

    /** Unicode emoji with a Discord shortcode of `:see_no_evil:`. Represented in Unicode as 🙈. */
    object SeeNoEvil : UnicodeEmoji("\ud83d\ude48")

    /** Unicode emoji with a Discord shortcode of `:hear_no_evil:`. Represented in Unicode as 🙉. */
    object HearNoEvil : UnicodeEmoji("\ud83d\ude49")

    /** Unicode emoji with a Discord shortcode of `:speak_no_evil:`. Represented in Unicode as 🙊. */
    object SpeakNoEvil : UnicodeEmoji("\ud83d\ude4a")

    /** Unicode emoji with a Discord shortcode of `:monkey:`. Represented in Unicode as 🐒. */
    object Monkey : UnicodeEmoji("\ud83d\udc12")

    /** Unicode emoji with a Discord shortcode of `:chicken:`. Represented in Unicode as 🐔. */
    object Chicken : UnicodeEmoji("\ud83d\udc14")

    /** Unicode emoji with a Discord shortcode of `:penguin:`. Represented in Unicode as 🐧. */
    object Penguin : UnicodeEmoji("\ud83d\udc27")

    /** Unicode emoji with a Discord shortcode of `:bird:`. Represented in Unicode as 🐦. */
    object Bird : UnicodeEmoji("\ud83d\udc26")

    /** Unicode emoji with a Discord shortcode of `:baby_chick:`. Represented in Unicode as 🐤. */
    object BabyChick : UnicodeEmoji("\ud83d\udc24")

    /** Unicode emoji with a Discord shortcode of `:hatching_chick:`. Represented in Unicode as 🐣. */
    object HatchingChick : UnicodeEmoji("\ud83d\udc23")

    /** Unicode emoji with a Discord shortcode of `:hatched_chick:`. Represented in Unicode as 🐥. */
    object HatchedChick : UnicodeEmoji("\ud83d\udc25")

    /** Unicode emoji with a Discord shortcode of `:wolf:`. Represented in Unicode as 🐺. */
    object Wolf : UnicodeEmoji("\ud83d\udc3a")

    /** Unicode emoji with a Discord shortcode of `:boar:`. Represented in Unicode as 🐗. */
    object Boar : UnicodeEmoji("\ud83d\udc17")

    /** Unicode emoji with a Discord shortcode of `:horse:`. Represented in Unicode as 🐴. */
    object Horse : UnicodeEmoji("\ud83d\udc34")

    /** Unicode emoji with a Discord shortcode of `:unicorn:`. Represented in Unicode as 🦄. */
    object Unicorn : UnicodeEmoji("\ud83e\udd84")

    /** Unicode emoji with a Discord shortcode of `:bee:`. Represented in Unicode as 🐝. */
    object Bee : UnicodeEmoji("\ud83d\udc1d")

    /** Unicode emoji with a Discord shortcode of `:bug:`. Represented in Unicode as 🐛. */
    object Bug : UnicodeEmoji("\ud83d\udc1b")

    /** Unicode emoji with a Discord shortcode of `:snail:`. Represented in Unicode as 🐌. */
    object Snail : UnicodeEmoji("\ud83d\udc0c")

    /** Unicode emoji with a Discord shortcode of `:beetle:`. Represented in Unicode as 🐞. */
    object Beetle : UnicodeEmoji("\ud83d\udc1e")

    /** Unicode emoji with a Discord shortcode of `:ant:`. Represented in Unicode as 🐜. */
    object Ant : UnicodeEmoji("\ud83d\udc1c")

    /** Unicode emoji with a Discord shortcode of `:spider:`. Represented in Unicode as 🕷. */
    object Spider : UnicodeEmoji("\ud83d\udd77")

    /** Unicode emoji with a Discord shortcode of `:scorpion:`. Represented in Unicode as 🦂. */
    object Scorpion : UnicodeEmoji("\ud83e\udd82")

    /** Unicode emoji with a Discord shortcode of `:crab:`. Represented in Unicode as 🦀. */
    object Crab : UnicodeEmoji("\ud83e\udd80")

    /** Unicode emoji with a Discord shortcode of `:snake:`. Represented in Unicode as 🐍. */
    object Snake : UnicodeEmoji("\ud83d\udc0d")

    /** Unicode emoji with a Discord shortcode of `:turtle:`. Represented in Unicode as 🐢. */
    object Turtle : UnicodeEmoji("\ud83d\udc22")

    /** Unicode emoji with a Discord shortcode of `:tropical_fish:`. Represented in Unicode as 🐠. */
    object TropicalFish : UnicodeEmoji("\ud83d\udc20")

    /** Unicode emoji with a Discord shortcode of `:fish:`. Represented in Unicode as 🐟. */
    object Fish : UnicodeEmoji("\ud83d\udc1f")

    /** Unicode emoji with a Discord shortcode of `:blowfish:`. Represented in Unicode as 🐡. */
    object Blowfish : UnicodeEmoji("\ud83d\udc21")

    /** Unicode emoji with a Discord shortcode of `:dolphin:`. Represented in Unicode as 🐬. */
    object Dolphin : UnicodeEmoji("\ud83d\udc2c")

    /** Unicode emoji with a Discord shortcode of `:whale:`. Represented in Unicode as 🐳. */
    object Whale : UnicodeEmoji("\ud83d\udc33")

    /** Unicode emoji with a Discord shortcode of `:whale2:`. Represented in Unicode as 🐋. */
    object Whale2 : UnicodeEmoji("\ud83d\udc0b")

    /** Unicode emoji with a Discord shortcode of `:crocodile:`. Represented in Unicode as 🐊. */
    object Crocodile : UnicodeEmoji("\ud83d\udc0a")

    /** Unicode emoji with a Discord shortcode of `:leopard:`. Represented in Unicode as 🐆. */
    object Leopard : UnicodeEmoji("\ud83d\udc06")

    /** Unicode emoji with a Discord shortcode of `:tiger2:`. Represented in Unicode as 🐅. */
    object Tiger2 : UnicodeEmoji("\ud83d\udc05")

    /** Unicode emoji with a Discord shortcode of `:water_buffalo:`. Represented in Unicode as 🐃. */
    object WaterBuffalo : UnicodeEmoji("\ud83d\udc03")

    /** Unicode emoji with a Discord shortcode of `:ox:`. Represented in Unicode as 🐂. */
    object Ox : UnicodeEmoji("\ud83d\udc02")

    /** Unicode emoji with a Discord shortcode of `:cow2:`. Represented in Unicode as 🐄. */
    object Cow2 : UnicodeEmoji("\ud83d\udc04")

    /** Unicode emoji with a Discord shortcode of `:dromedary_camel:`. Represented in Unicode as 🐪. */
    object DromedaryCamel : UnicodeEmoji("\ud83d\udc2a")

    /** Unicode emoji with a Discord shortcode of `:camel:`. Represented in Unicode as 🐫. */
    object Camel : UnicodeEmoji("\ud83d\udc2b")

    /** Unicode emoji with a Discord shortcode of `:elephant:`. Represented in Unicode as 🐘. */
    object Elephant : UnicodeEmoji("\ud83d\udc18")

    /** Unicode emoji with a Discord shortcode of `:goat:`. Represented in Unicode as 🐐. */
    object Goat : UnicodeEmoji("\ud83d\udc10")

    /** Unicode emoji with a Discord shortcode of `:ram:`. Represented in Unicode as 🐏. */
    object Ram : UnicodeEmoji("\ud83d\udc0f")

    /** Unicode emoji with a Discord shortcode of `:sheep:`. Represented in Unicode as 🐑. */
    object Sheep : UnicodeEmoji("\ud83d\udc11")

    /** Unicode emoji with a Discord shortcode of `:racehorse:`. Represented in Unicode as 🐎. */
    object Racehorse : UnicodeEmoji("\ud83d\udc0e")

    /** Unicode emoji with a Discord shortcode of `:pig2:`. Represented in Unicode as 🐖. */
    object Pig2 : UnicodeEmoji("\ud83d\udc16")

    /** Unicode emoji with a Discord shortcode of `:rat:`. Represented in Unicode as 🐀. */
    object Rat : UnicodeEmoji("\ud83d\udc00")

    /** Unicode emoji with a Discord shortcode of `:mouse2:`. Represented in Unicode as 🐁. */
    object Mouse2 : UnicodeEmoji("\ud83d\udc01")

    /** Unicode emoji with a Discord shortcode of `:rooster:`. Represented in Unicode as 🐓. */
    object Rooster : UnicodeEmoji("\ud83d\udc13")

    /** Unicode emoji with a Discord shortcode of `:turkey:`. Represented in Unicode as 🦃. */
    object Turkey : UnicodeEmoji("\ud83e\udd83")

    /** Unicode emoji with a Discord shortcode of `:dove:`. Represented in Unicode as 🕊. */
    object Dove : UnicodeEmoji("\ud83d\udd4a")

    /** Unicode emoji with a Discord shortcode of `:dog2:`. Represented in Unicode as 🐕. */
    object Dog2 : UnicodeEmoji("\ud83d\udc15")

    /** Unicode emoji with a Discord shortcode of `:poodle:`. Represented in Unicode as 🐩. */
    object Poodle : UnicodeEmoji("\ud83d\udc29")

    /** Unicode emoji with a Discord shortcode of `:cat2:`. Represented in Unicode as 🐈. */
    object Cat2 : UnicodeEmoji("\ud83d\udc08")

    /** Unicode emoji with a Discord shortcode of `:rabbit2:`. Represented in Unicode as 🐇. */
    object Rabbit2 : UnicodeEmoji("\ud83d\udc07")

    /** Unicode emoji with a Discord shortcode of `:chipmunk:`. Represented in Unicode as 🐿. */
    object Chipmunk : UnicodeEmoji("\ud83d\udc3f")

    /** Unicode emoji with a Discord shortcode of `:paw_prints:`. Represented in Unicode as 🐾. */
    object PawPrints : UnicodeEmoji("\ud83d\udc3e")

    /** Unicode emoji with a Discord shortcode of `:dragon:`. Represented in Unicode as 🐉. */
    object Dragon : UnicodeEmoji("\ud83d\udc09")

    /** Unicode emoji with a Discord shortcode of `:dragon_face:`. Represented in Unicode as 🐲. */
    object DragonFace : UnicodeEmoji("\ud83d\udc32")

    /** Unicode emoji with a Discord shortcode of `:cactus:`. Represented in Unicode as 🌵. */
    object Cactus : UnicodeEmoji("\ud83c\udf35")

    /** Unicode emoji with a Discord shortcode of `:christmas_tree:`. Represented in Unicode as 🎄. */
    object ChristmasTree : UnicodeEmoji("\ud83c\udf84")

    /** Unicode emoji with a Discord shortcode of `:evergreen_tree:`. Represented in Unicode as 🌲. */
    object EvergreenTree : UnicodeEmoji("\ud83c\udf32")

    /** Unicode emoji with a Discord shortcode of `:deciduous_tree:`. Represented in Unicode as 🌳. */
    object DeciduousTree : UnicodeEmoji("\ud83c\udf33")

    /** Unicode emoji with a Discord shortcode of `:palm_tree:`. Represented in Unicode as 🌴. */
    object PalmTree : UnicodeEmoji("\ud83c\udf34")

    /** Unicode emoji with a Discord shortcode of `:seedling:`. Represented in Unicode as 🌱. */
    object Seedling : UnicodeEmoji("\ud83c\udf31")

    /** Unicode emoji with a Discord shortcode of `:herb:`. Represented in Unicode as 🌿. */
    object Herb : UnicodeEmoji("\ud83c\udf3f")

    /** Unicode emoji with a Discord shortcode of `:shamrock:`. Represented in Unicode as ☘. */
    object Shamrock : UnicodeEmoji("\u2618")

    /** Unicode emoji with a Discord shortcode of `:four_leaf_clover:`. Represented in Unicode as 🍀. */
    object FourLeafClover : UnicodeEmoji("\ud83c\udf40")

    /** Unicode emoji with a Discord shortcode of `:bamboo:`. Represented in Unicode as 🎍. */
    object Bamboo : UnicodeEmoji("\ud83c\udf8d")

    /** Unicode emoji with a Discord shortcode of `:tanabata_tree:`. Represented in Unicode as 🎋. */
    object TanabataTree : UnicodeEmoji("\ud83c\udf8b")

    /** Unicode emoji with a Discord shortcode of `:leaves:`. Represented in Unicode as 🍃. */
    object Leaves : UnicodeEmoji("\ud83c\udf43")

    /** Unicode emoji with a Discord shortcode of `:fallen_leaf:`. Represented in Unicode as 🍂. */
    object FallenLeaf : UnicodeEmoji("\ud83c\udf42")

    /** Unicode emoji with a Discord shortcode of `:maple_leaf:`. Represented in Unicode as 🍁. */
    object MapleLeaf : UnicodeEmoji("\ud83c\udf41")

    /** Unicode emoji with a Discord shortcode of `:ear_of_rice:`. Represented in Unicode as 🌾. */
    object EarOfRice : UnicodeEmoji("\ud83c\udf3e")

    /** Unicode emoji with a Discord shortcode of `:hibiscus:`. Represented in Unicode as 🌺. */
    object Hibiscus : UnicodeEmoji("\ud83c\udf3a")

    /** Unicode emoji with a Discord shortcode of `:sunflower:`. Represented in Unicode as 🌻. */
    object Sunflower : UnicodeEmoji("\ud83c\udf3b")

    /** Unicode emoji with a Discord shortcode of `:rose:`. Represented in Unicode as 🌹. */
    object Rose : UnicodeEmoji("\ud83c\udf39")

    /** Unicode emoji with a Discord shortcode of `:tulip:`. Represented in Unicode as 🌷. */
    object Tulip : UnicodeEmoji("\ud83c\udf37")

    /** Unicode emoji with a Discord shortcode of `:blossom:`. Represented in Unicode as 🌼. */
    object Blossom : UnicodeEmoji("\ud83c\udf3c")

    /** Unicode emoji with a Discord shortcode of `:cherry_blossom:`. Represented in Unicode as 🌸. */
    object CherryBlossom : UnicodeEmoji("\ud83c\udf38")

    /** Unicode emoji with a Discord shortcode of `:bouquet:`. Represented in Unicode as 💐. */
    object Bouquet : UnicodeEmoji("\ud83d\udc90")

    /** Unicode emoji with a Discord shortcode of `:mushroom:`. Represented in Unicode as 🍄. */
    object Mushroom : UnicodeEmoji("\ud83c\udf44")

    /** Unicode emoji with a Discord shortcode of `:chestnut:`. Represented in Unicode as 🌰. */
    object Chestnut : UnicodeEmoji("\ud83c\udf30")

    /** Unicode emoji with a Discord shortcode of `:jack_o_lantern:`. Represented in Unicode as 🎃. */
    object JackOLantern : UnicodeEmoji("\ud83c\udf83")

    /** Unicode emoji with a Discord shortcode of `:shell:`. Represented in Unicode as 🐚. */
    object Shell : UnicodeEmoji("\ud83d\udc1a")

    /** Unicode emoji with a Discord shortcode of `:spider_web:`. Represented in Unicode as 🕸. */
    object SpiderWeb : UnicodeEmoji("\ud83d\udd78")

    /** Unicode emoji with a Discord shortcode of `:earth_americas:`. Represented in Unicode as 🌎. */
    object EarthAmericas : UnicodeEmoji("\ud83c\udf0e")

    /** Unicode emoji with a Discord shortcode of `:earth_africa:`. Represented in Unicode as 🌍. */
    object EarthAfrica : UnicodeEmoji("\ud83c\udf0d")

    /** Unicode emoji with a Discord shortcode of `:earth_asia:`. Represented in Unicode as 🌏. */
    object EarthAsia : UnicodeEmoji("\ud83c\udf0f")

    /** Unicode emoji with a Discord shortcode of `:full_moon:`. Represented in Unicode as 🌕. */
    object FullMoon : UnicodeEmoji("\ud83c\udf15")

    /** Unicode emoji with a Discord shortcode of `:waning_gibbous_moon:`. Represented in Unicode as 🌖. */
    object WaningGibbousMoon : UnicodeEmoji("\ud83c\udf16")

    /** Unicode emoji with a Discord shortcode of `:last_quarter_moon:`. Represented in Unicode as 🌗. */
    object LastQuarterMoon : UnicodeEmoji("\ud83c\udf17")

    /** Unicode emoji with a Discord shortcode of `:waning_crescent_moon:`. Represented in Unicode as 🌘. */
    object WaningCrescentMoon : UnicodeEmoji("\ud83c\udf18")

    /** Unicode emoji with a Discord shortcode of `:new_moon:`. Represented in Unicode as 🌑. */
    object NewMoon : UnicodeEmoji("\ud83c\udf11")

    /** Unicode emoji with a Discord shortcode of `:waxing_crescent_moon:`. Represented in Unicode as 🌒. */
    object WaxingCrescentMoon : UnicodeEmoji("\ud83c\udf12")

    /** Unicode emoji with a Discord shortcode of `:first_quarter_moon:`. Represented in Unicode as 🌓. */
    object FirstQuarterMoon : UnicodeEmoji("\ud83c\udf13")

    /** Unicode emoji with a Discord shortcode of `:moon:`. Represented in Unicode as 🌔. */
    object Moon : UnicodeEmoji("\ud83c\udf14")

    /** Unicode emoji with a Discord shortcode of `:new_moon_with_face:`. Represented in Unicode as 🌚. */
    object NewMoonWithFace : UnicodeEmoji("\ud83c\udf1a")

    /** Unicode emoji with a Discord shortcode of `:full_moon_with_face:`. Represented in Unicode as 🌝. */
    object FullMoonWithFace : UnicodeEmoji("\ud83c\udf1d")

    /** Unicode emoji with a Discord shortcode of `:first_quarter_moon_with_face:`. Represented in Unicode as 🌛. */
    object FirstQuarterMoonWithFace : UnicodeEmoji("\ud83c\udf1b")

    /** Unicode emoji with a Discord shortcode of `:last_quarter_moon_with_face:`. Represented in Unicode as 🌜. */
    object LastQuarterMoonWithFace : UnicodeEmoji("\ud83c\udf1c")

    /** Unicode emoji with a Discord shortcode of `:sun_with_face:`. Represented in Unicode as 🌞. */
    object SunWithFace : UnicodeEmoji("\ud83c\udf1e")

    /** Unicode emoji with a Discord shortcode of `:crescent_moon:`. Represented in Unicode as 🌙. */
    object CrescentMoon : UnicodeEmoji("\ud83c\udf19")

    /** Unicode emoji with a Discord shortcode of `:star:`. Represented in Unicode as ⭐. */
    object Star : UnicodeEmoji("\u2b50")

    /** Unicode emoji with a Discord shortcode of `:star2:`. Represented in Unicode as 🌟. */
    object Star2 : UnicodeEmoji("\ud83c\udf1f")

    /** Unicode emoji with a Discord shortcode of `:dizzy:`. Represented in Unicode as 💫. */
    object Dizzy : UnicodeEmoji("\ud83d\udcab")

    /** Unicode emoji with a Discord shortcode of `:sparkles:`. Represented in Unicode as ✨. */
    object Sparkles : UnicodeEmoji("\u2728")

    /** Unicode emoji with a Discord shortcode of `:comet:`. Represented in Unicode as ☄. */
    object Comet : UnicodeEmoji("\u2604")

    /** Unicode emoji with a Discord shortcode of `:sunny:`. Represented in Unicode as ☀. */
    object Sunny : UnicodeEmoji("\u2600")

    /** Unicode emoji with a Discord shortcode of `:white_sun_small_cloud:`. Represented in Unicode as 🌤. */
    object WhiteSunSmallCloud : UnicodeEmoji("\ud83c\udf24")

    /** Unicode emoji with a Discord shortcode of `:partly_sunny:`. Represented in Unicode as ⛅. */
    object PartlySunny : UnicodeEmoji("\u26c5")

    /** Unicode emoji with a Discord shortcode of `:white_sun_cloud:`. Represented in Unicode as 🌥. */
    object WhiteSunCloud : UnicodeEmoji("\ud83c\udf25")

    /** Unicode emoji with a Discord shortcode of `:white_sun_rain_cloud:`. Represented in Unicode as 🌦. */
    object WhiteSunRainCloud : UnicodeEmoji("\ud83c\udf26")

    /** Unicode emoji with a Discord shortcode of `:cloud:`. Represented in Unicode as ☁. */
    object Cloud : UnicodeEmoji("\u2601")

    /** Unicode emoji with a Discord shortcode of `:cloud_rain:`. Represented in Unicode as 🌧. */
    object CloudRain : UnicodeEmoji("\ud83c\udf27")

    /** Unicode emoji with a Discord shortcode of `:thunder_cloud_rain:`. Represented in Unicode as ⛈. */
    object ThunderCloudRain : UnicodeEmoji("\u26c8")

    /** Unicode emoji with a Discord shortcode of `:cloud_lightning:`. Represented in Unicode as 🌩. */
    object CloudLightning : UnicodeEmoji("\ud83c\udf29")

    /** Unicode emoji with a Discord shortcode of `:zap:`. Represented in Unicode as ⚡. */
    object Zap : UnicodeEmoji("\u26a1")

    /** Unicode emoji with a Discord shortcode of `:fire:`. Represented in Unicode as 🔥. */
    object Fire : UnicodeEmoji("\ud83d\udd25")

    /** Unicode emoji with a Discord shortcode of `:boom:`. Represented in Unicode as 💥. */
    object Boom : UnicodeEmoji("\ud83d\udca5")

    /** Unicode emoji with a Discord shortcode of `:snowflake:`. Represented in Unicode as ❄. */
    object Snowflake : UnicodeEmoji("\u2744")

    /** Unicode emoji with a Discord shortcode of `:cloud_snow:`. Represented in Unicode as 🌨. */
    object CloudSnow : UnicodeEmoji("\ud83c\udf28")

    /** Unicode emoji with a Discord shortcode of `:snowman2:`. Represented in Unicode as ☃. */
    object Snowman2 : UnicodeEmoji("\u2603")

    /** Unicode emoji with a Discord shortcode of `:snowman:`. Represented in Unicode as ⛄. */
    object Snowman : UnicodeEmoji("\u26c4")

    /** Unicode emoji with a Discord shortcode of `:wind_blowing_face:`. Represented in Unicode as 🌬. */
    object WindBlowingFace : UnicodeEmoji("\ud83c\udf2c")

    /** Unicode emoji with a Discord shortcode of `:dash:`. Represented in Unicode as 💨. */
    object Dash : UnicodeEmoji("\ud83d\udca8")

    /** Unicode emoji with a Discord shortcode of `:cloud_tornado:`. Represented in Unicode as 🌪. */
    object CloudTornado : UnicodeEmoji("\ud83c\udf2a")

    /** Unicode emoji with a Discord shortcode of `:fog:`. Represented in Unicode as 🌫. */
    object Fog : UnicodeEmoji("\ud83c\udf2b")

    /** Unicode emoji with a Discord shortcode of `:umbrella2:`. Represented in Unicode as ☂. */
    object Umbrella2 : UnicodeEmoji("\u2602")

    /** Unicode emoji with a Discord shortcode of `:umbrella:`. Represented in Unicode as ☔. */
    object Umbrella : UnicodeEmoji("\u2614")

    /** Unicode emoji with a Discord shortcode of `:droplet:`. Represented in Unicode as 💧. */
    object Droplet : UnicodeEmoji("\ud83d\udca7")

    /** Unicode emoji with a Discord shortcode of `:sweat_drops:`. Represented in Unicode as 💦. */
    object SweatDrops : UnicodeEmoji("\ud83d\udca6")

    /** Unicode emoji with a Discord shortcode of `:ocean:`. Represented in Unicode as 🌊. */
    object Ocean : UnicodeEmoji("\ud83c\udf0a")

    /** Unicode emoji with a Discord shortcode of `:eagle:`. Represented in Unicode as 🦅. */
    object Eagle : UnicodeEmoji("\ud83e\udd85")

    /** Unicode emoji with a Discord shortcode of `:duck:`. Represented in Unicode as 🦆. */
    object Duck : UnicodeEmoji("\ud83e\udd86")

    /** Unicode emoji with a Discord shortcode of `:bat:`. Represented in Unicode as 🦇. */
    object Bat : UnicodeEmoji("\ud83e\udd87")

    /** Unicode emoji with a Discord shortcode of `:shark:`. Represented in Unicode as 🦈. */
    object Shark : UnicodeEmoji("\ud83e\udd88")

    /** Unicode emoji with a Discord shortcode of `:owl:`. Represented in Unicode as 🦉. */
    object Owl : UnicodeEmoji("\ud83e\udd89")

    /** Unicode emoji with a Discord shortcode of `:fox:`. Represented in Unicode as 🦊. */
    object Fox : UnicodeEmoji("\ud83e\udd8a")

    /** Unicode emoji with a Discord shortcode of `:butterfly:`. Represented in Unicode as 🦋. */
    object Butterfly : UnicodeEmoji("\ud83e\udd8b")

    /** Unicode emoji with a Discord shortcode of `:deer:`. Represented in Unicode as 🦌. */
    object Deer : UnicodeEmoji("\ud83e\udd8c")

    /** Unicode emoji with a Discord shortcode of `:gorilla:`. Represented in Unicode as 🦍. */
    object Gorilla : UnicodeEmoji("\ud83e\udd8d")

    /** Unicode emoji with a Discord shortcode of `:lizard:`. Represented in Unicode as 🦎. */
    object Lizard : UnicodeEmoji("\ud83e\udd8e")

    /** Unicode emoji with a Discord shortcode of `:rhino:`. Represented in Unicode as 🦏. */
    object Rhino : UnicodeEmoji("\ud83e\udd8f")

    /** Unicode emoji with a Discord shortcode of `:wilted_flower:`. Represented in Unicode as 🥀. */
    object WiltedFlower : UnicodeEmoji("\ud83e\udd40")

    /** Unicode emoji with a Discord shortcode of `:shrimp:`. Represented in Unicode as 🦐. */
    object Shrimp : UnicodeEmoji("\ud83e\udd90")

    /** Unicode emoji with a Discord shortcode of `:squid:`. Represented in Unicode as 🦑. */
    object Squid : UnicodeEmoji("\ud83e\udd91")

    // Food

    /** Unicode emoji with a Discord shortcode of `:green_apple:`. Represented in Unicode as 🍏. */
    object GreenApple : UnicodeEmoji("\ud83c\udf4f")

    /** Unicode emoji with a Discord shortcode of `:apple:`. Represented in Unicode as 🍎. */
    object Apple : UnicodeEmoji("\ud83c\udf4e")

    /** Unicode emoji with a Discord shortcode of `:pear:`. Represented in Unicode as 🍐. */
    object Pear : UnicodeEmoji("\ud83c\udf50")

    /** Unicode emoji with a Discord shortcode of `:tangerine:`. Represented in Unicode as 🍊. */
    object Tangerine : UnicodeEmoji("\ud83c\udf4a")

    /** Unicode emoji with a Discord shortcode of `:lemon:`. Represented in Unicode as 🍋. */
    object Lemon : UnicodeEmoji("\ud83c\udf4b")

    /** Unicode emoji with a Discord shortcode of `:banana:`. Represented in Unicode as 🍌. */
    object Banana : UnicodeEmoji("\ud83c\udf4c")

    /** Unicode emoji with a Discord shortcode of `:watermelon:`. Represented in Unicode as 🍉. */
    object Watermelon : UnicodeEmoji("\ud83c\udf49")

    /** Unicode emoji with a Discord shortcode of `:grapes:`. Represented in Unicode as 🍇. */
    object Grapes : UnicodeEmoji("\ud83c\udf47")

    /** Unicode emoji with a Discord shortcode of `:strawberry:`. Represented in Unicode as 🍓. */
    object Strawberry : UnicodeEmoji("\ud83c\udf53")

    /** Unicode emoji with a Discord shortcode of `:melon:`. Represented in Unicode as 🍈. */
    object Melon : UnicodeEmoji("\ud83c\udf48")

    /** Unicode emoji with a Discord shortcode of `:cherries:`. Represented in Unicode as 🍒. */
    object Cherries : UnicodeEmoji("\ud83c\udf52")

    /** Unicode emoji with a Discord shortcode of `:peach:`. Represented in Unicode as 🍑. */
    object Peach : UnicodeEmoji("\ud83c\udf51")

    /** Unicode emoji with a Discord shortcode of `:pineapple:`. Represented in Unicode as 🍍. */
    object Pineapple : UnicodeEmoji("\ud83c\udf4d")

    /** Unicode emoji with a Discord shortcode of `:tomato:`. Represented in Unicode as 🍅. */
    object Tomato : UnicodeEmoji("\ud83c\udf45")

    /** Unicode emoji with a Discord shortcode of `:eggplant:`. Represented in Unicode as 🍆. */
    object Eggplant : UnicodeEmoji("\ud83c\udf46")

    /** Unicode emoji with a Discord shortcode of `:hot_pepper:`. Represented in Unicode as 🌶. */
    object HotPepper : UnicodeEmoji("\ud83c\udf36")

    /** Unicode emoji with a Discord shortcode of `:corn:`. Represented in Unicode as 🌽. */
    object Corn : UnicodeEmoji("\ud83c\udf3d")

    /** Unicode emoji with a Discord shortcode of `:sweet_potato:`. Represented in Unicode as 🍠. */
    object SweetPotato : UnicodeEmoji("\ud83c\udf60")

    /** Unicode emoji with a Discord shortcode of `:honey_pot:`. Represented in Unicode as 🍯. */
    object HoneyPot : UnicodeEmoji("\ud83c\udf6f")

    /** Unicode emoji with a Discord shortcode of `:bread:`. Represented in Unicode as 🍞. */
    object Bread : UnicodeEmoji("\ud83c\udf5e")

    /** Unicode emoji with a Discord shortcode of `:cheese:`. Represented in Unicode as 🧀. */
    object Cheese : UnicodeEmoji("\ud83e\uddc0")

    /** Unicode emoji with a Discord shortcode of `:poultry_leg:`. Represented in Unicode as 🍗. */
    object PoultryLeg : UnicodeEmoji("\ud83c\udf57")

    /** Unicode emoji with a Discord shortcode of `:meat_on_bone:`. Represented in Unicode as 🍖. */
    object MeatOnBone : UnicodeEmoji("\ud83c\udf56")

    /** Unicode emoji with a Discord shortcode of `:fried_shrimp:`. Represented in Unicode as 🍤. */
    object FriedShrimp : UnicodeEmoji("\ud83c\udf64")

    /** Unicode emoji with a Discord shortcode of `:cooking:`. Represented in Unicode as 🍳. */
    object Cooking : UnicodeEmoji("\ud83c\udf73")

    /** Unicode emoji with a Discord shortcode of `:hamburger:`. Represented in Unicode as 🍔. */
    object Hamburger : UnicodeEmoji("\ud83c\udf54")

    /** Unicode emoji with a Discord shortcode of `:fries:`. Represented in Unicode as 🍟. */
    object Fries : UnicodeEmoji("\ud83c\udf5f")

    /** Unicode emoji with a Discord shortcode of `:hotdog:`. Represented in Unicode as 🌭. */
    object Hotdog : UnicodeEmoji("\ud83c\udf2d")

    /** Unicode emoji with a Discord shortcode of `:pizza:`. Represented in Unicode as 🍕. */
    object Pizza : UnicodeEmoji("\ud83c\udf55")

    /** Unicode emoji with a Discord shortcode of `:spaghetti:`. Represented in Unicode as 🍝. */
    object Spaghetti : UnicodeEmoji("\ud83c\udf5d")

    /** Unicode emoji with a Discord shortcode of `:taco:`. Represented in Unicode as 🌮. */
    object Taco : UnicodeEmoji("\ud83c\udf2e")

    /** Unicode emoji with a Discord shortcode of `:burrito:`. Represented in Unicode as 🌯. */
    object Burrito : UnicodeEmoji("\ud83c\udf2f")

    /** Unicode emoji with a Discord shortcode of `:ramen:`. Represented in Unicode as 🍜. */
    object Ramen : UnicodeEmoji("\ud83c\udf5c")

    /** Unicode emoji with a Discord shortcode of `:stew:`. Represented in Unicode as 🍲. */
    object Stew : UnicodeEmoji("\ud83c\udf72")

    /** Unicode emoji with a Discord shortcode of `:fish_cake:`. Represented in Unicode as 🍥. */
    object FishCake : UnicodeEmoji("\ud83c\udf65")

    /** Unicode emoji with a Discord shortcode of `:sushi:`. Represented in Unicode as 🍣. */
    object Sushi : UnicodeEmoji("\ud83c\udf63")

    /** Unicode emoji with a Discord shortcode of `:bento:`. Represented in Unicode as 🍱. */
    object Bento : UnicodeEmoji("\ud83c\udf71")

    /** Unicode emoji with a Discord shortcode of `:curry:`. Represented in Unicode as 🍛. */
    object Curry : UnicodeEmoji("\ud83c\udf5b")

    /** Unicode emoji with a Discord shortcode of `:rice_ball:`. Represented in Unicode as 🍙. */
    object RiceBall : UnicodeEmoji("\ud83c\udf59")

    /** Unicode emoji with a Discord shortcode of `:rice:`. Represented in Unicode as 🍚. */
    object Rice : UnicodeEmoji("\ud83c\udf5a")

    /** Unicode emoji with a Discord shortcode of `:rice_cracker:`. Represented in Unicode as 🍘. */
    object RiceCracker : UnicodeEmoji("\ud83c\udf58")

    /** Unicode emoji with a Discord shortcode of `:oden:`. Represented in Unicode as 🍢. */
    object Oden : UnicodeEmoji("\ud83c\udf62")

    /** Unicode emoji with a Discord shortcode of `:dango:`. Represented in Unicode as 🍡. */
    object Dango : UnicodeEmoji("\ud83c\udf61")

    /** Unicode emoji with a Discord shortcode of `:shaved_ice:`. Represented in Unicode as 🍧. */
    object ShavedIce : UnicodeEmoji("\ud83c\udf67")

    /** Unicode emoji with a Discord shortcode of `:ice_cream:`. Represented in Unicode as 🍦. */
    object IceCream : UnicodeEmoji("\ud83c\udf66")

    /** Unicode emoji with a Discord shortcode of `:cake:`. Represented in Unicode as 🍰. */
    object Cake : UnicodeEmoji("\ud83c\udf70")

    /** Unicode emoji with a Discord shortcode of `:birthday:`. Represented in Unicode as 🎂. */
    object Birthday : UnicodeEmoji("\ud83c\udf82")

    /** Unicode emoji with a Discord shortcode of `:custard:`. Represented in Unicode as 🍮. */
    object Custard : UnicodeEmoji("\ud83c\udf6e")

    /** Unicode emoji with a Discord shortcode of `:candy:`. Represented in Unicode as 🍬. */
    object Candy : UnicodeEmoji("\ud83c\udf6c")

    /** Unicode emoji with a Discord shortcode of `:lollipop:`. Represented in Unicode as 🍭. */
    object Lollipop : UnicodeEmoji("\ud83c\udf6d")

    /** Unicode emoji with a Discord shortcode of `:chocolate_bar:`. Represented in Unicode as 🍫. */
    object ChocolateBar : UnicodeEmoji("\ud83c\udf6b")

    /** Unicode emoji with a Discord shortcode of `:popcorn:`. Represented in Unicode as 🍿. */
    object Popcorn : UnicodeEmoji("\ud83c\udf7f")

    /** Unicode emoji with a Discord shortcode of `:doughnut:`. Represented in Unicode as 🍩. */
    object Doughnut : UnicodeEmoji("\ud83c\udf69")

    /** Unicode emoji with a Discord shortcode of `:cookie:`. Represented in Unicode as 🍪. */
    object Cookie : UnicodeEmoji("\ud83c\udf6a")

    /** Unicode emoji with a Discord shortcode of `:beer:`. Represented in Unicode as 🍺. */
    object Beer : UnicodeEmoji("\ud83c\udf7a")

    /** Unicode emoji with a Discord shortcode of `:beers:`. Represented in Unicode as 🍻. */
    object Beers : UnicodeEmoji("\ud83c\udf7b")

    /** Unicode emoji with a Discord shortcode of `:wine_glass:`. Represented in Unicode as 🍷. */
    object WineGlass : UnicodeEmoji("\ud83c\udf77")

    /** Unicode emoji with a Discord shortcode of `:cocktail:`. Represented in Unicode as 🍸. */
    object Cocktail : UnicodeEmoji("\ud83c\udf78")

    /** Unicode emoji with a Discord shortcode of `:tropical_drink:`. Represented in Unicode as 🍹. */
    object TropicalDrink : UnicodeEmoji("\ud83c\udf79")

    /** Unicode emoji with a Discord shortcode of `:champagne:`. Represented in Unicode as 🍾. */
    object Champagne : UnicodeEmoji("\ud83c\udf7e")

    /** Unicode emoji with a Discord shortcode of `:sake:`. Represented in Unicode as 🍶. */
    object Sake : UnicodeEmoji("\ud83c\udf76")

    /** Unicode emoji with a Discord shortcode of `:tea:`. Represented in Unicode as 🍵. */
    object Tea : UnicodeEmoji("\ud83c\udf75")

    /** Unicode emoji with a Discord shortcode of `:coffee:`. Represented in Unicode as ☕. */
    object Coffee : UnicodeEmoji("\u2615")

    /** Unicode emoji with a Discord shortcode of `:baby_bottle:`. Represented in Unicode as 🍼. */
    object BabyBottle : UnicodeEmoji("\ud83c\udf7c")

    /** Unicode emoji with a Discord shortcode of `:fork_and_knife:`. Represented in Unicode as 🍴. */
    object ForkAndKnife : UnicodeEmoji("\ud83c\udf74")

    /** Unicode emoji with a Discord shortcode of `:fork_knife_plate:`. Represented in Unicode as 🍽. */
    object ForkKnifePlate : UnicodeEmoji("\ud83c\udf7d")

    /** Unicode emoji with a Discord shortcode of `:croissant:`. Represented in Unicode as 🥐. */
    object Croissant : UnicodeEmoji("\ud83e\udd50")

    /** Unicode emoji with a Discord shortcode of `:avocado:`. Represented in Unicode as 🥑. */
    object Avocado : UnicodeEmoji("\ud83e\udd51")

    /** Unicode emoji with a Discord shortcode of `:cucumber:`. Represented in Unicode as 🥒. */
    object Cucumber : UnicodeEmoji("\ud83e\udd52")

    /** Unicode emoji with a Discord shortcode of `:bacon:`. Represented in Unicode as 🥓. */
    object Bacon : UnicodeEmoji("\ud83e\udd53")

    /** Unicode emoji with a Discord shortcode of `:potato:`. Represented in Unicode as 🥔. */
    object Potato : UnicodeEmoji("\ud83e\udd54")

    /** Unicode emoji with a Discord shortcode of `:carrot:`. Represented in Unicode as 🥕. */
    object Carrot : UnicodeEmoji("\ud83e\udd55")

    /** Unicode emoji with a Discord shortcode of `:french_bread:`. Represented in Unicode as 🥖. */
    object FrenchBread : UnicodeEmoji("\ud83e\udd56")

    /** Unicode emoji with a Discord shortcode of `:salad:`. Represented in Unicode as 🥗. */
    object Salad : UnicodeEmoji("\ud83e\udd57")

    /** Unicode emoji with a Discord shortcode of `:paella:`. Represented in Unicode as 🥘. */
    object Paella : UnicodeEmoji("\ud83e\udd58")

    /** Unicode emoji with a Discord shortcode of `:stuffed_flatbread:`. Represented in Unicode as 🥙. */
    object StuffedFlatbread : UnicodeEmoji("\ud83e\udd59")

    /** Unicode emoji with a Discord shortcode of `:clinking_glass:`. Represented in Unicode as 🥂. */
    object ClinkingGlass : UnicodeEmoji("\ud83e\udd42")

    /** Unicode emoji with a Discord shortcode of `:whisky:`. Represented in Unicode as 🥃. */
    object Whisky : UnicodeEmoji("\ud83e\udd43")

    /** Unicode emoji with a Discord shortcode of `:spoon:`. Represented in Unicode as 🥄. */
    object Spoon : UnicodeEmoji("\ud83e\udd44")

    /** Unicode emoji with a Discord shortcode of `:egg:`. Represented in Unicode as 🥚. */
    object Egg : UnicodeEmoji("\ud83e\udd5a")

    /** Unicode emoji with a Discord shortcode of `:milk:`. Represented in Unicode as 🥛. */
    object Milk : UnicodeEmoji("\ud83e\udd5b")

    /** Unicode emoji with a Discord shortcode of `:peanuts:`. Represented in Unicode as 🥜. */
    object Peanuts : UnicodeEmoji("\ud83e\udd5c")

    /** Unicode emoji with a Discord shortcode of `:kiwi:`. Represented in Unicode as 🥝. */
    object Kiwi : UnicodeEmoji("\ud83e\udd5d")

    /** Unicode emoji with a Discord shortcode of `:pancakes:`. Represented in Unicode as 🥞. */
    object Pancakes : UnicodeEmoji("\ud83e\udd5e")

    // Activities

    /** Unicode emoji with a Discord shortcode of `:soccer:`. Represented in Unicode as ⚽. */
    object Soccer : UnicodeEmoji("\u26bd")

    /** Unicode emoji with a Discord shortcode of `:basketball:`. Represented in Unicode as 🏀. */
    object Basketball : UnicodeEmoji("\ud83c\udfc0")

    /** Unicode emoji with a Discord shortcode of `:football:`. Represented in Unicode as 🏈. */
    object Football : UnicodeEmoji("\ud83c\udfc8")

    /** Unicode emoji with a Discord shortcode of `:baseball:`. Represented in Unicode as ⚾. */
    object Baseball : UnicodeEmoji("\u26be")

    /** Unicode emoji with a Discord shortcode of `:tennis:`. Represented in Unicode as 🎾. */
    object Tennis : UnicodeEmoji("\ud83c\udfbe")

    /** Unicode emoji with a Discord shortcode of `:volleyball:`. Represented in Unicode as 🏐. */
    object Volleyball : UnicodeEmoji("\ud83c\udfd0")

    /** Unicode emoji with a Discord shortcode of `:rugby_football:`. Represented in Unicode as 🏉. */
    object RugbyFootball : UnicodeEmoji("\ud83c\udfc9")

    /** Unicode emoji with a Discord shortcode of `:eight_ball:`. Represented in Unicode as 🎱. */
    object EightBall : UnicodeEmoji("\ud83c\udfb1")

    /** Unicode emoji with a Discord shortcode of `:golf:`. Represented in Unicode as ⛳. */
    object Golf : UnicodeEmoji("\u26f3")

    /** Unicode emoji with a Discord shortcode of `:golfer:`. Represented in Unicode as 🏌. */
    object Golfer : UnicodeEmoji("\ud83c\udfcc")

    /** Unicode emoji with a Discord shortcode of `:ping_pong:`. Represented in Unicode as 🏓. */
    object PingPong : UnicodeEmoji("\ud83c\udfd3")

    /** Unicode emoji with a Discord shortcode of `:badminton:`. Represented in Unicode as 🏸. */
    object Badminton : UnicodeEmoji("\ud83c\udff8")

    /** Unicode emoji with a Discord shortcode of `:hockey:`. Represented in Unicode as 🏒. */
    object Hockey : UnicodeEmoji("\ud83c\udfd2")

    /** Unicode emoji with a Discord shortcode of `:field_hockey:`. Represented in Unicode as 🏑. */
    object FieldHockey : UnicodeEmoji("\ud83c\udfd1")

    /** Unicode emoji with a Discord shortcode of `:cricket:`. Represented in Unicode as 🏏. */
    object Cricket : UnicodeEmoji("\ud83c\udfcf")

    /** Unicode emoji with a Discord shortcode of `:ski:`. Represented in Unicode as 🎿. */
    object Ski : UnicodeEmoji("\ud83c\udfbf")

    /** Unicode emoji with a Discord shortcode of `:skier:`. Represented in Unicode as ⛷. */
    object Skier : UnicodeEmoji("\u26f7")

    /** Unicode emoji with a Discord shortcode of `:snowboarder:`. Represented in Unicode as 🏂. */
    object Snowboarder : UnicodeEmoji("\ud83c\udfc2")

    /** Unicode emoji with a Discord shortcode of `:ice_skate:`. Represented in Unicode as ⛸. */
    object IceSkate : UnicodeEmoji("\u26f8")

    /** Unicode emoji with a Discord shortcode of `:bow_and_arrow:`. Represented in Unicode as 🏹. */
    object BowAndArrow : UnicodeEmoji("\ud83c\udff9")

    /** Unicode emoji with a Discord shortcode of `:fishing_pole_and_fish:`. Represented in Unicode as 🎣. */
    object FishingPoleAndFish : UnicodeEmoji("\ud83c\udfa3")

    /** Unicode emoji with a Discord shortcode of `:rowboat:`. Represented in Unicode as 🚣. */
    object Rowboat : UnicodeEmoji("\ud83d\udea3")

    /** Unicode emoji with a Discord shortcode of `:swimmer:`. Represented in Unicode as 🏊. */
    object Swimmer : UnicodeEmoji("\ud83c\udfca")

    /** Unicode emoji with a Discord shortcode of `:surfer:`. Represented in Unicode as 🏄. */
    object Surfer : UnicodeEmoji("\ud83c\udfc4")

    /** Unicode emoji with a Discord shortcode of `:bath:`. Represented in Unicode as 🛀. */
    object Bath : UnicodeEmoji("\ud83d\udec0")

    /** Unicode emoji with a Discord shortcode of `:basketball_player:`. Represented in Unicode as ⛹. */
    object BasketballPlayer : UnicodeEmoji("\u26f9")

    /** Unicode emoji with a Discord shortcode of `:lifter:`. Represented in Unicode as 🏋. */
    object Lifter : UnicodeEmoji("\ud83c\udfcb")

    /** Unicode emoji with a Discord shortcode of `:bicyclist:`. Represented in Unicode as 🚴. */
    object Bicyclist : UnicodeEmoji("\ud83d\udeb4")

    /** Unicode emoji with a Discord shortcode of `:mountain_bicyclist:`. Represented in Unicode as 🚵. */
    object MountainBicyclist : UnicodeEmoji("\ud83d\udeb5")

    /** Unicode emoji with a Discord shortcode of `:horse_racing:`. Represented in Unicode as 🏇. */
    object HorseRacing : UnicodeEmoji("\ud83c\udfc7")

    /** Unicode emoji with a Discord shortcode of `:levitate:`. Represented in Unicode as 🕴. */
    object Levitate : UnicodeEmoji("\ud83d\udd74")

    /** Unicode emoji with a Discord shortcode of `:trophy:`. Represented in Unicode as 🏆. */
    object Trophy : UnicodeEmoji("\ud83c\udfc6")

    /** Unicode emoji with a Discord shortcode of `:running_shirt_with_sash:`. Represented in Unicode as 🎽. */
    object RunningShirtWithSash : UnicodeEmoji("\ud83c\udfbd")

    /** Unicode emoji with a Discord shortcode of `:medal:`. Represented in Unicode as 🏅. */
    object Medal : UnicodeEmoji("\ud83c\udfc5")

    /** Unicode emoji with a Discord shortcode of `:military_medal:`. Represented in Unicode as 🎖. */
    object MilitaryMedal : UnicodeEmoji("\ud83c\udf96")

    /** Unicode emoji with a Discord shortcode of `:reminder_ribbon:`. Represented in Unicode as 🎗. */
    object ReminderRibbon : UnicodeEmoji("\ud83c\udf97")

    /** Unicode emoji with a Discord shortcode of `:rosette:`. Represented in Unicode as 🏵. */
    object Rosette : UnicodeEmoji("\ud83c\udff5")

    /** Unicode emoji with a Discord shortcode of `:ticket:`. Represented in Unicode as 🎫. */
    object Ticket : UnicodeEmoji("\ud83c\udfab")

    /** Unicode emoji with a Discord shortcode of `:tickets:`. Represented in Unicode as 🎟. */
    object Tickets : UnicodeEmoji("\ud83c\udf9f")

    /** Unicode emoji with a Discord shortcode of `:performing_arts:`. Represented in Unicode as 🎭. */
    object PerformingArts : UnicodeEmoji("\ud83c\udfad")

    /** Unicode emoji with a Discord shortcode of `:art:`. Represented in Unicode as 🎨. */
    object Art : UnicodeEmoji("\ud83c\udfa8")

    /** Unicode emoji with a Discord shortcode of `:circus_tent:`. Represented in Unicode as 🎪. */
    object CircusTent : UnicodeEmoji("\ud83c\udfaa")

    /** Unicode emoji with a Discord shortcode of `:microphone:`. Represented in Unicode as 🎤. */
    object Microphone : UnicodeEmoji("\ud83c\udfa4")

    /** Unicode emoji with a Discord shortcode of `:headphones:`. Represented in Unicode as 🎧. */
    object Headphones : UnicodeEmoji("\ud83c\udfa7")

    /** Unicode emoji with a Discord shortcode of `:musical_score:`. Represented in Unicode as 🎼. */
    object MusicalScore : UnicodeEmoji("\ud83c\udfbc")

    /** Unicode emoji with a Discord shortcode of `:musical_keyboard:`. Represented in Unicode as 🎹. */
    object MusicalKeyboard : UnicodeEmoji("\ud83c\udfb9")

    /** Unicode emoji with a Discord shortcode of `:saxophone:`. Represented in Unicode as 🎷. */
    object Saxophone : UnicodeEmoji("\ud83c\udfb7")

    /** Unicode emoji with a Discord shortcode of `:trumpet:`. Represented in Unicode as 🎺. */
    object Trumpet : UnicodeEmoji("\ud83c\udfba")

    /** Unicode emoji with a Discord shortcode of `:guitar:`. Represented in Unicode as 🎸. */
    object Guitar : UnicodeEmoji("\ud83c\udfb8")

    /** Unicode emoji with a Discord shortcode of `:violin:`. Represented in Unicode as 🎻. */
    object Violin : UnicodeEmoji("\ud83c\udfbb")

    /** Unicode emoji with a Discord shortcode of `:clapper:`. Represented in Unicode as 🎬. */
    object Clapper : UnicodeEmoji("\ud83c\udfac")

    /** Unicode emoji with a Discord shortcode of `:video_game:`. Represented in Unicode as 🎮. */
    object VideoGame : UnicodeEmoji("\ud83c\udfae")

    /** Unicode emoji with a Discord shortcode of `:space_invader:`. Represented in Unicode as 👾. */
    object SpaceInvader : UnicodeEmoji("\ud83d\udc7e")

    /** Unicode emoji with a Discord shortcode of `:dart:`. Represented in Unicode as 🎯. */
    object Dart : UnicodeEmoji("\ud83c\udfaf")

    /** Unicode emoji with a Discord shortcode of `:game_die:`. Represented in Unicode as 🎲. */
    object GameDie : UnicodeEmoji("\ud83c\udfb2")

    /** Unicode emoji with a Discord shortcode of `:slot_machine:`. Represented in Unicode as 🎰. */
    object SlotMachine : UnicodeEmoji("\ud83c\udfb0")

    /** Unicode emoji with a Discord shortcode of `:bowling:`. Represented in Unicode as 🎳. */
    object Bowling : UnicodeEmoji("\ud83c\udfb3")

    /** Unicode emoji with a Discord shortcode of `:cartwheel:`. Represented in Unicode as 🤸. */
    object Cartwheel : UnicodeEmoji("\ud83e\udd38")

    /** Unicode emoji with a Discord shortcode of `:juggling:`. Represented in Unicode as 🤹. */
    object Juggling : UnicodeEmoji("\ud83e\udd39")

    /** Unicode emoji with a Discord shortcode of `:wrestlers:`. Represented in Unicode as 🤼. */
    object Wrestlers : UnicodeEmoji("\ud83e\udd3c")

    /** Unicode emoji with a Discord shortcode of `:boxing_glove:`. Represented in Unicode as 🥊. */
    object BoxingGlove : UnicodeEmoji("\ud83e\udd4a")

    /** Unicode emoji with a Discord shortcode of `:martial_arts_uniform:`. Represented in Unicode as 🥋. */
    object MartialArtsUniform : UnicodeEmoji("\ud83e\udd4b")

    /** Unicode emoji with a Discord shortcode of `:water_polo:`. Represented in Unicode as 🤽. */
    object WaterPolo : UnicodeEmoji("\ud83e\udd3d")

    /** Unicode emoji with a Discord shortcode of `:handball:`. Represented in Unicode as 🤾. */
    object Handball : UnicodeEmoji("\ud83e\udd3e")

    /** Unicode emoji with a Discord shortcode of `:goal:`. Represented in Unicode as 🥅. */
    object Goal : UnicodeEmoji("\ud83e\udd45")

    /** Unicode emoji with a Discord shortcode of `:fencer:`. Represented in Unicode as 🤺. */
    object Fencer : UnicodeEmoji("\ud83e\udd3a")

    /** Unicode emoji with a Discord shortcode of `:first_place:`. Represented in Unicode as 🥇. */
    object FirstPlace : UnicodeEmoji("\ud83e\udd47")

    /** Unicode emoji with a Discord shortcode of `:second_place:`. Represented in Unicode as 🥈. */
    object SecondPlace : UnicodeEmoji("\ud83e\udd48")

    /** Unicode emoji with a Discord shortcode of `:third_place:`. Represented in Unicode as 🥉. */
    object ThirdPlace : UnicodeEmoji("\ud83e\udd49")

    /** Unicode emoji with a Discord shortcode of `:drum:`. Represented in Unicode as 🥁. */
    object Drum : UnicodeEmoji("\ud83e\udd41")

    // Travel

    /** Unicode emoji with a Discord shortcode of `:car:`. Represented in Unicode as 🚗. */
    object Car : UnicodeEmoji("\ud83d\ude97")

    /** Unicode emoji with a Discord shortcode of `:taxi:`. Represented in Unicode as 🚕. */
    object Taxi : UnicodeEmoji("\ud83d\ude95")

    /** Unicode emoji with a Discord shortcode of `:blue_car:`. Represented in Unicode as 🚙. */
    object BlueCar : UnicodeEmoji("\ud83d\ude99")

    /** Unicode emoji with a Discord shortcode of `:bus:`. Represented in Unicode as 🚌. */
    object Bus : UnicodeEmoji("\ud83d\ude8c")

    /** Unicode emoji with a Discord shortcode of `:trolleybus:`. Represented in Unicode as 🚎. */
    object Trolleybus : UnicodeEmoji("\ud83d\ude8e")

    /** Unicode emoji with a Discord shortcode of `:race_car:`. Represented in Unicode as 🏎. */
    object RaceCar : UnicodeEmoji("\ud83c\udfce")

    /** Unicode emoji with a Discord shortcode of `:police_car:`. Represented in Unicode as 🚓. */
    object PoliceCar : UnicodeEmoji("\ud83d\ude93")

    /** Unicode emoji with a Discord shortcode of `:ambulance:`. Represented in Unicode as 🚑. */
    object Ambulance : UnicodeEmoji("\ud83d\ude91")

    /** Unicode emoji with a Discord shortcode of `:fire_engine:`. Represented in Unicode as 🚒. */
    object FireEngine : UnicodeEmoji("\ud83d\ude92")

    /** Unicode emoji with a Discord shortcode of `:minibus:`. Represented in Unicode as 🚐. */
    object Minibus : UnicodeEmoji("\ud83d\ude90")

    /** Unicode emoji with a Discord shortcode of `:truck:`. Represented in Unicode as 🚚. */
    object Truck : UnicodeEmoji("\ud83d\ude9a")

    /** Unicode emoji with a Discord shortcode of `:articulated_lorry:`. Represented in Unicode as 🚛. */
    object ArticulatedLorry : UnicodeEmoji("\ud83d\ude9b")

    /** Unicode emoji with a Discord shortcode of `:tractor:`. Represented in Unicode as 🚜. */
    object Tractor : UnicodeEmoji("\ud83d\ude9c")

    /** Unicode emoji with a Discord shortcode of `:motorcycle:`. Represented in Unicode as 🏍. */
    object Motorcycle : UnicodeEmoji("\ud83c\udfcd")

    /** Unicode emoji with a Discord shortcode of `:bike:`. Represented in Unicode as 🚲. */
    object Bike : UnicodeEmoji("\ud83d\udeb2")

    /** Unicode emoji with a Discord shortcode of `:rotating_light:`. Represented in Unicode as 🚨. */
    object RotatingLight : UnicodeEmoji("\ud83d\udea8")

    /** Unicode emoji with a Discord shortcode of `:oncoming_police_car:`. Represented in Unicode as 🚔. */
    object OncomingPoliceCar : UnicodeEmoji("\ud83d\ude94")

    /** Unicode emoji with a Discord shortcode of `:oncoming_bus:`. Represented in Unicode as 🚍. */
    object OncomingBus : UnicodeEmoji("\ud83d\ude8d")

    /** Unicode emoji with a Discord shortcode of `:oncoming_automobile:`. Represented in Unicode as 🚘. */
    object OncomingAutomobile : UnicodeEmoji("\ud83d\ude98")

    /** Unicode emoji with a Discord shortcode of `:oncoming_taxi:`. Represented in Unicode as 🚖. */
    object OncomingTaxi : UnicodeEmoji("\ud83d\ude96")

    /** Unicode emoji with a Discord shortcode of `:aerial_tramway:`. Represented in Unicode as 🚡. */
    object AerialTramway : UnicodeEmoji("\ud83d\udea1")

    /** Unicode emoji with a Discord shortcode of `:mountain_cableway:`. Represented in Unicode as 🚠. */
    object MountainCableway : UnicodeEmoji("\ud83d\udea0")

    /** Unicode emoji with a Discord shortcode of `:suspension_railway:`. Represented in Unicode as 🚟. */
    object SuspensionRailway : UnicodeEmoji("\ud83d\ude9f")

    /** Unicode emoji with a Discord shortcode of `:railway_car:`. Represented in Unicode as 🚃. */
    object RailwayCar : UnicodeEmoji("\ud83d\ude83")

    /** Unicode emoji with a Discord shortcode of `:train:`. Represented in Unicode as 🚋. */
    object Train : UnicodeEmoji("\ud83d\ude8b")

    /** Unicode emoji with a Discord shortcode of `:monorail:`. Represented in Unicode as 🚝. */
    object Monorail : UnicodeEmoji("\ud83d\ude9d")

    /** Unicode emoji with a Discord shortcode of `:bullettrain_side:`. Represented in Unicode as 🚄. */
    object BullettrainSide : UnicodeEmoji("\ud83d\ude84")

    /** Unicode emoji with a Discord shortcode of `:bullettrain_front:`. Represented in Unicode as 🚅. */
    object BullettrainFront : UnicodeEmoji("\ud83d\ude85")

    /** Unicode emoji with a Discord shortcode of `:light_rail:`. Represented in Unicode as 🚈. */
    object LightRail : UnicodeEmoji("\ud83d\ude88")

    /** Unicode emoji with a Discord shortcode of `:mountain_railway:`. Represented in Unicode as 🚞. */
    object MountainRailway : UnicodeEmoji("\ud83d\ude9e")

    /** Unicode emoji with a Discord shortcode of `:steam_locomotive:`. Represented in Unicode as 🚂. */
    object SteamLocomotive : UnicodeEmoji("\ud83d\ude82")

    /** Unicode emoji with a Discord shortcode of `:train2:`. Represented in Unicode as 🚆. */
    object Train2 : UnicodeEmoji("\ud83d\ude86")

    /** Unicode emoji with a Discord shortcode of `:metro:`. Represented in Unicode as 🚇. */
    object Metro : UnicodeEmoji("\ud83d\ude87")

    /** Unicode emoji with a Discord shortcode of `:tram:`. Represented in Unicode as 🚊. */
    object Tram : UnicodeEmoji("\ud83d\ude8a")

    /** Unicode emoji with a Discord shortcode of `:station:`. Represented in Unicode as 🚉. */
    object Station : UnicodeEmoji("\ud83d\ude89")

    /** Unicode emoji with a Discord shortcode of `:helicopter:`. Represented in Unicode as 🚁. */
    object Helicopter : UnicodeEmoji("\ud83d\ude81")

    /** Unicode emoji with a Discord shortcode of `:airplane_small:`. Represented in Unicode as 🛩. */
    object AirplaneSmall : UnicodeEmoji("\ud83d\udee9")

    /** Unicode emoji with a Discord shortcode of `:airplane:`. Represented in Unicode as ✈. */
    object Airplane : UnicodeEmoji("\u2708")

    /** Unicode emoji with a Discord shortcode of `:airplane_departure:`. Represented in Unicode as 🛫. */
    object AirplaneDeparture : UnicodeEmoji("\ud83d\udeeb")

    /** Unicode emoji with a Discord shortcode of `:airplane_arriving:`. Represented in Unicode as 🛬. */
    object AirplaneArriving : UnicodeEmoji("\ud83d\udeec")

    /** Unicode emoji with a Discord shortcode of `:sailboat:`. Represented in Unicode as ⛵. */
    object Sailboat : UnicodeEmoji("\u26f5")

    /** Unicode emoji with a Discord shortcode of `:motorboat:`. Represented in Unicode as 🛥. */
    object Motorboat : UnicodeEmoji("\ud83d\udee5")

    /** Unicode emoji with a Discord shortcode of `:speedboat:`. Represented in Unicode as 🚤. */
    object Speedboat : UnicodeEmoji("\ud83d\udea4")

    /** Unicode emoji with a Discord shortcode of `:ferry:`. Represented in Unicode as ⛴. */
    object Ferry : UnicodeEmoji("\u26f4")

    /** Unicode emoji with a Discord shortcode of `:cruise_ship:`. Represented in Unicode as 🛳. */
    object CruiseShip : UnicodeEmoji("\ud83d\udef3")

    /** Unicode emoji with a Discord shortcode of `:rocket:`. Represented in Unicode as 🚀. */
    object Rocket : UnicodeEmoji("\ud83d\ude80")

    /** Unicode emoji with a Discord shortcode of `:satellite_orbital:`. Represented in Unicode as 🛰. */
    object SatelliteOrbital : UnicodeEmoji("\ud83d\udef0")

    /** Unicode emoji with a Discord shortcode of `:seat:`. Represented in Unicode as 💺. */
    object Seat : UnicodeEmoji("\ud83d\udcba")

    /** Unicode emoji with a Discord shortcode of `:anchor:`. Represented in Unicode as ⚓. */
    object Anchor : UnicodeEmoji("\u2693")

    /** Unicode emoji with a Discord shortcode of `:construction:`. Represented in Unicode as 🚧. */
    object Construction : UnicodeEmoji("\ud83d\udea7")

    /** Unicode emoji with a Discord shortcode of `:fuelpump:`. Represented in Unicode as ⛽. */
    object Fuelpump : UnicodeEmoji("\u26fd")

    /** Unicode emoji with a Discord shortcode of `:busstop:`. Represented in Unicode as 🚏. */
    object Busstop : UnicodeEmoji("\ud83d\ude8f")

    /** Unicode emoji with a Discord shortcode of `:vertical_traffic_light:`. Represented in Unicode as 🚦. */
    object VerticalTrafficLight : UnicodeEmoji("\ud83d\udea6")

    /** Unicode emoji with a Discord shortcode of `:traffic_light:`. Represented in Unicode as 🚥. */
    object TrafficLight : UnicodeEmoji("\ud83d\udea5")

    /** Unicode emoji with a Discord shortcode of `:checkered_flag:`. Represented in Unicode as 🏁. */
    object CheckeredFlag : UnicodeEmoji("\ud83c\udfc1")

    /** Unicode emoji with a Discord shortcode of `:ship:`. Represented in Unicode as 🚢. */
    object Ship : UnicodeEmoji("\ud83d\udea2")

    /** Unicode emoji with a Discord shortcode of `:ferris_wheel:`. Represented in Unicode as 🎡. */
    object FerrisWheel : UnicodeEmoji("\ud83c\udfa1")

    /** Unicode emoji with a Discord shortcode of `:roller_coaster:`. Represented in Unicode as 🎢. */
    object RollerCoaster : UnicodeEmoji("\ud83c\udfa2")

    /** Unicode emoji with a Discord shortcode of `:carousel_horse:`. Represented in Unicode as 🎠. */
    object CarouselHorse : UnicodeEmoji("\ud83c\udfa0")

    /** Unicode emoji with a Discord shortcode of `:construction_site:`. Represented in Unicode as 🏗. */
    object ConstructionSite : UnicodeEmoji("\ud83c\udfd7")

    /** Unicode emoji with a Discord shortcode of `:foggy:`. Represented in Unicode as 🌁. */
    object Foggy : UnicodeEmoji("\ud83c\udf01")

    /** Unicode emoji with a Discord shortcode of `:tokyo_tower:`. Represented in Unicode as 🗼. */
    object TokyoTower : UnicodeEmoji("\ud83d\uddfc")

    /** Unicode emoji with a Discord shortcode of `:factory:`. Represented in Unicode as 🏭. */
    object Factory : UnicodeEmoji("\ud83c\udfed")

    /** Unicode emoji with a Discord shortcode of `:fountain:`. Represented in Unicode as ⛲. */
    object Fountain : UnicodeEmoji("\u26f2")

    /** Unicode emoji with a Discord shortcode of `:rice_scene:`. Represented in Unicode as 🎑. */
    object RiceScene : UnicodeEmoji("\ud83c\udf91")

    /** Unicode emoji with a Discord shortcode of `:mountain:`. Represented in Unicode as ⛰. */
    object Mountain : UnicodeEmoji("\u26f0")

    /** Unicode emoji with a Discord shortcode of `:mountain_snow:`. Represented in Unicode as 🏔. */
    object MountainSnow : UnicodeEmoji("\ud83c\udfd4")

    /** Unicode emoji with a Discord shortcode of `:mount_fuji:`. Represented in Unicode as 🗻. */
    object MountFuji : UnicodeEmoji("\ud83d\uddfb")

    /** Unicode emoji with a Discord shortcode of `:volcano:`. Represented in Unicode as 🌋. */
    object Volcano : UnicodeEmoji("\ud83c\udf0b")

    /** Unicode emoji with a Discord shortcode of `:japan:`. Represented in Unicode as 🗾. */
    object Japan : UnicodeEmoji("\ud83d\uddfe")

    /** Unicode emoji with a Discord shortcode of `:camping:`. Represented in Unicode as 🏕. */
    object Camping : UnicodeEmoji("\ud83c\udfd5")

    /** Unicode emoji with a Discord shortcode of `:tent:`. Represented in Unicode as ⛺. */
    object Tent : UnicodeEmoji("\u26fa")

    /** Unicode emoji with a Discord shortcode of `:park:`. Represented in Unicode as 🏞. */
    object Park : UnicodeEmoji("\ud83c\udfde")

    /** Unicode emoji with a Discord shortcode of `:motorway:`. Represented in Unicode as 🛣. */
    object Motorway : UnicodeEmoji("\ud83d\udee3")

    /** Unicode emoji with a Discord shortcode of `:railway_track:`. Represented in Unicode as 🛤. */
    object RailwayTrack : UnicodeEmoji("\ud83d\udee4")

    /** Unicode emoji with a Discord shortcode of `:sunrise:`. Represented in Unicode as 🌅. */
    object Sunrise : UnicodeEmoji("\ud83c\udf05")

    /** Unicode emoji with a Discord shortcode of `:sunrise_over_mountains:`. Represented in Unicode as 🌄. */
    object SunriseOverMountains : UnicodeEmoji("\ud83c\udf04")

    /** Unicode emoji with a Discord shortcode of `:desert:`. Represented in Unicode as 🏜. */
    object Desert : UnicodeEmoji("\ud83c\udfdc")

    /** Unicode emoji with a Discord shortcode of `:beach:`. Represented in Unicode as 🏖. */
    object Beach : UnicodeEmoji("\ud83c\udfd6")

    /** Unicode emoji with a Discord shortcode of `:island:`. Represented in Unicode as 🏝. */
    object Island : UnicodeEmoji("\ud83c\udfdd")

    /** Unicode emoji with a Discord shortcode of `:city_sunrise:`. Represented in Unicode as 🌇. */
    object CitySunrise : UnicodeEmoji("\ud83c\udf07")

    /** Unicode emoji with a Discord shortcode of `:city_dusk:`. Represented in Unicode as 🌆. */
    object CityDusk : UnicodeEmoji("\ud83c\udf06")

    /** Unicode emoji with a Discord shortcode of `:cityscape:`. Represented in Unicode as 🏙. */
    object Cityscape : UnicodeEmoji("\ud83c\udfd9")

    /** Unicode emoji with a Discord shortcode of `:night_with_stars:`. Represented in Unicode as 🌃. */
    object NightWithStars : UnicodeEmoji("\ud83c\udf03")

    /** Unicode emoji with a Discord shortcode of `:bridge_at_night:`. Represented in Unicode as 🌉. */
    object BridgeAtNight : UnicodeEmoji("\ud83c\udf09")

    /** Unicode emoji with a Discord shortcode of `:milky_way:`. Represented in Unicode as 🌌. */
    object MilkyWay : UnicodeEmoji("\ud83c\udf0c")

    /** Unicode emoji with a Discord shortcode of `:stars:`. Represented in Unicode as 🌠. */
    object Stars : UnicodeEmoji("\ud83c\udf20")

    /** Unicode emoji with a Discord shortcode of `:sparkler:`. Represented in Unicode as 🎇. */
    object Sparkler : UnicodeEmoji("\ud83c\udf87")

    /** Unicode emoji with a Discord shortcode of `:fireworks:`. Represented in Unicode as 🎆. */
    object Fireworks : UnicodeEmoji("\ud83c\udf86")

    /** Unicode emoji with a Discord shortcode of `:rainbow:`. Represented in Unicode as 🌈. */
    object Rainbow : UnicodeEmoji("\ud83c\udf08")

    /** Unicode emoji with a Discord shortcode of `:homes:`. Represented in Unicode as 🏘. */
    object Homes : UnicodeEmoji("\ud83c\udfd8")

    /** Unicode emoji with a Discord shortcode of `:european_castle:`. Represented in Unicode as 🏰. */
    object EuropeanCastle : UnicodeEmoji("\ud83c\udff0")

    /** Unicode emoji with a Discord shortcode of `:japanese_castle:`. Represented in Unicode as 🏯. */
    object JapaneseCastle : UnicodeEmoji("\ud83c\udfef")

    /** Unicode emoji with a Discord shortcode of `:stadium:`. Represented in Unicode as 🏟. */
    object Stadium : UnicodeEmoji("\ud83c\udfdf")

    /** Unicode emoji with a Discord shortcode of `:statue_of_liberty:`. Represented in Unicode as 🗽. */
    object StatueOfLiberty : UnicodeEmoji("\ud83d\uddfd")

    /** Unicode emoji with a Discord shortcode of `:house:`. Represented in Unicode as 🏠. */
    object House : UnicodeEmoji("\ud83c\udfe0")

    /** Unicode emoji with a Discord shortcode of `:house_with_garden:`. Represented in Unicode as 🏡. */
    object HouseWithGarden : UnicodeEmoji("\ud83c\udfe1")

    /** Unicode emoji with a Discord shortcode of `:house_abandoned:`. Represented in Unicode as 🏚. */
    object HouseAbandoned : UnicodeEmoji("\ud83c\udfda")

    /** Unicode emoji with a Discord shortcode of `:office:`. Represented in Unicode as 🏢. */
    object Office : UnicodeEmoji("\ud83c\udfe2")

    /** Unicode emoji with a Discord shortcode of `:department_store:`. Represented in Unicode as 🏬. */
    object DepartmentStore : UnicodeEmoji("\ud83c\udfec")

    /** Unicode emoji with a Discord shortcode of `:post_office:`. Represented in Unicode as 🏣. */
    object PostOffice : UnicodeEmoji("\ud83c\udfe3")

    /** Unicode emoji with a Discord shortcode of `:european_post_office:`. Represented in Unicode as 🏤. */
    object EuropeanPostOffice : UnicodeEmoji("\ud83c\udfe4")

    /** Unicode emoji with a Discord shortcode of `:hospital:`. Represented in Unicode as 🏥. */
    object Hospital : UnicodeEmoji("\ud83c\udfe5")

    /** Unicode emoji with a Discord shortcode of `:bank:`. Represented in Unicode as 🏦. */
    object Bank : UnicodeEmoji("\ud83c\udfe6")

    /** Unicode emoji with a Discord shortcode of `:hotel:`. Represented in Unicode as 🏨. */
    object Hotel : UnicodeEmoji("\ud83c\udfe8")

    /** Unicode emoji with a Discord shortcode of `:convenience_store:`. Represented in Unicode as 🏪. */
    object ConvenienceStore : UnicodeEmoji("\ud83c\udfea")

    /** Unicode emoji with a Discord shortcode of `:school:`. Represented in Unicode as 🏫. */
    object School : UnicodeEmoji("\ud83c\udfeb")

    /** Unicode emoji with a Discord shortcode of `:love_hotel:`. Represented in Unicode as 🏩. */
    object LoveHotel : UnicodeEmoji("\ud83c\udfe9")

    /** Unicode emoji with a Discord shortcode of `:wedding:`. Represented in Unicode as 💒. */
    object Wedding : UnicodeEmoji("\ud83d\udc92")

    /** Unicode emoji with a Discord shortcode of `:classical_building:`. Represented in Unicode as 🏛. */
    object ClassicalBuilding : UnicodeEmoji("\ud83c\udfdb")

    /** Unicode emoji with a Discord shortcode of `:church:`. Represented in Unicode as ⛪. */
    object Church : UnicodeEmoji("\u26ea")

    /** Unicode emoji with a Discord shortcode of `:mosque:`. Represented in Unicode as 🕌. */
    object Mosque : UnicodeEmoji("\ud83d\udd4c")

    /** Unicode emoji with a Discord shortcode of `:synagogue:`. Represented in Unicode as 🕍. */
    object Synagogue : UnicodeEmoji("\ud83d\udd4d")

    /** Unicode emoji with a Discord shortcode of `:kaaba:`. Represented in Unicode as 🕋. */
    object Kaaba : UnicodeEmoji("\ud83d\udd4b")

    /** Unicode emoji with a Discord shortcode of `:shinto_shrine:`. Represented in Unicode as ⛩. */
    object ShintoShrine : UnicodeEmoji("\u26e9")

    /** Unicode emoji with a Discord shortcode of `:scooter:`. Represented in Unicode as 🛴. */
    object Scooter : UnicodeEmoji("\ud83d\udef4")

    /** Unicode emoji with a Discord shortcode of `:motorbike:`. Represented in Unicode as 🛵. */
    object Motorbike : UnicodeEmoji("\ud83d\udef5")

    /** Unicode emoji with a Discord shortcode of `:canoe:`. Represented in Unicode as 🛶. */
    object Canoe : UnicodeEmoji("\ud83d\udef6")

    // Objects

    /** Unicode emoji with a Discord shortcode of `:watch:`. Represented in Unicode as ⌚. */
    object Watch : UnicodeEmoji("\u231a")

    /** Unicode emoji with a Discord shortcode of `:iphone:`. Represented in Unicode as 📱. */
    object Iphone : UnicodeEmoji("\ud83d\udcf1")

    /** Unicode emoji with a Discord shortcode of `:calling:`. Represented in Unicode as 📲. */
    object Calling : UnicodeEmoji("\ud83d\udcf2")

    /** Unicode emoji with a Discord shortcode of `:computer:`. Represented in Unicode as 💻. */
    object Computer : UnicodeEmoji("\ud83d\udcbb")

    /** Unicode emoji with a Discord shortcode of `:keyboard:`. Represented in Unicode as ⌨. */
    object Keyboard : UnicodeEmoji("\u2328")

    /** Unicode emoji with a Discord shortcode of `:desktop:`. Represented in Unicode as 🖥. */
    object Desktop : UnicodeEmoji("\ud83d\udda5")

    /** Unicode emoji with a Discord shortcode of `:printer:`. Represented in Unicode as 🖨. */
    object Printer : UnicodeEmoji("\ud83d\udda8")

    /** Unicode emoji with a Discord shortcode of `:mouse_three_button:`. Represented in Unicode as 🖱. */
    object MouseThreeButton : UnicodeEmoji("\ud83d\uddb1")

    /** Unicode emoji with a Discord shortcode of `:trackball:`. Represented in Unicode as 🖲. */
    object Trackball : UnicodeEmoji("\ud83d\uddb2")

    /** Unicode emoji with a Discord shortcode of `:joystick:`. Represented in Unicode as 🕹. */
    object Joystick : UnicodeEmoji("\ud83d\udd79")

    /** Unicode emoji with a Discord shortcode of `:compression:`. Represented in Unicode as 🗜. */
    object Compression : UnicodeEmoji("\ud83d\udddc")

    /** Unicode emoji with a Discord shortcode of `:minidisc:`. Represented in Unicode as 💽. */
    object Minidisc : UnicodeEmoji("\ud83d\udcbd")

    /** Unicode emoji with a Discord shortcode of `:floppy_disk:`. Represented in Unicode as 💾. */
    object FloppyDisk : UnicodeEmoji("\ud83d\udcbe")

    /** Unicode emoji with a Discord shortcode of `:cd:`. Represented in Unicode as 💿. */
    object CD : UnicodeEmoji("\ud83d\udcbf")

    /** Unicode emoji with a Discord shortcode of `:dvd:`. Represented in Unicode as 📀. */
    object Dvd : UnicodeEmoji("\ud83d\udcc0")

    /** Unicode emoji with a Discord shortcode of `:vhs:`. Represented in Unicode as 📼. */
    object Vhs : UnicodeEmoji("\ud83d\udcfc")

    /** Unicode emoji with a Discord shortcode of `:camera:`. Represented in Unicode as 📷. */
    object Camera : UnicodeEmoji("\ud83d\udcf7")

    /** Unicode emoji with a Discord shortcode of `:camera_with_flash:`. Represented in Unicode as 📸. */
    object CameraWithFlash : UnicodeEmoji("\ud83d\udcf8")

    /** Unicode emoji with a Discord shortcode of `:video_camera:`. Represented in Unicode as 📹. */
    object VideoCamera : UnicodeEmoji("\ud83d\udcf9")

    /** Unicode emoji with a Discord shortcode of `:movie_camera:`. Represented in Unicode as 🎥. */
    object MovieCamera : UnicodeEmoji("\ud83c\udfa5")

    /** Unicode emoji with a Discord shortcode of `:projector:`. Represented in Unicode as 📽. */
    object Projector : UnicodeEmoji("\ud83d\udcfd")

    /** Unicode emoji with a Discord shortcode of `:film_frames:`. Represented in Unicode as 🎞. */
    object FilmFrames : UnicodeEmoji("\ud83c\udf9e")

    /** Unicode emoji with a Discord shortcode of `:telephone_receiver:`. Represented in Unicode as 📞. */
    object TelephoneReceiver : UnicodeEmoji("\ud83d\udcde")

    /** Unicode emoji with a Discord shortcode of `:telephone:`. Represented in Unicode as ☎. */
    object Telephone : UnicodeEmoji("\u260e")

    /** Unicode emoji with a Discord shortcode of `:pager:`. Represented in Unicode as 📟. */
    object Pager : UnicodeEmoji("\ud83d\udcdf")

    /** Unicode emoji with a Discord shortcode of `:fax:`. Represented in Unicode as 📠. */
    object Fax : UnicodeEmoji("\ud83d\udce0")

    /** Unicode emoji with a Discord shortcode of `:tv:`. Represented in Unicode as 📺. */
    object TV : UnicodeEmoji("\ud83d\udcfa")

    /** Unicode emoji with a Discord shortcode of `:radio:`. Represented in Unicode as 📻. */
    object Radio : UnicodeEmoji("\ud83d\udcfb")

    /** Unicode emoji with a Discord shortcode of `:microphone2:`. Represented in Unicode as 🎙. */
    object Microphone2 : UnicodeEmoji("\ud83c\udf99")

    /** Unicode emoji with a Discord shortcode of `:level_slider:`. Represented in Unicode as 🎚. */
    object LevelSlider : UnicodeEmoji("\ud83c\udf9a")

    /** Unicode emoji with a Discord shortcode of `:control_knobs:`. Represented in Unicode as 🎛. */
    object ControlKnobs : UnicodeEmoji("\ud83c\udf9b")

    /** Unicode emoji with a Discord shortcode of `:stopwatch:`. Represented in Unicode as ⏱. */
    object Stopwatch : UnicodeEmoji("\u23f1")

    /** Unicode emoji with a Discord shortcode of `:timer:`. Represented in Unicode as ⏲. */
    object Timer : UnicodeEmoji("\u23f2")

    /** Unicode emoji with a Discord shortcode of `:alarm_clock:`. Represented in Unicode as ⏰. */
    object AlarmClock : UnicodeEmoji("\u23f0")

    /** Unicode emoji with a Discord shortcode of `:clock:`. Represented in Unicode as 🕰. */
    object Clock : UnicodeEmoji("\ud83d\udd70")

    /** Unicode emoji with a Discord shortcode of `:hourglass_flowing_sand:`. Represented in Unicode as ⏳. */
    object HourglassFlowingSand : UnicodeEmoji("\u23f3")

    /** Unicode emoji with a Discord shortcode of `:hourglass:`. Represented in Unicode as ⌛. */
    object Hourglass : UnicodeEmoji("\u231b")

    /** Unicode emoji with a Discord shortcode of `:satellite:`. Represented in Unicode as 📡. */
    object Satellite : UnicodeEmoji("\ud83d\udce1")

    /** Unicode emoji with a Discord shortcode of `:battery:`. Represented in Unicode as 🔋. */
    object Battery : UnicodeEmoji("\ud83d\udd0b")

    /** Unicode emoji with a Discord shortcode of `:electric_plug:`. Represented in Unicode as 🔌. */
    object ElectricPlug : UnicodeEmoji("\ud83d\udd0c")

    /** Unicode emoji with a Discord shortcode of `:bulb:`. Represented in Unicode as 💡. */
    object Bulb : UnicodeEmoji("\ud83d\udca1")

    /** Unicode emoji with a Discord shortcode of `:flashlight:`. Represented in Unicode as 🔦. */
    object Flashlight : UnicodeEmoji("\ud83d\udd26")

    /** Unicode emoji with a Discord shortcode of `:candle:`. Represented in Unicode as 🕯. */
    object Candle : UnicodeEmoji("\ud83d\udd6f")

    /** Unicode emoji with a Discord shortcode of `:wastebasket:`. Represented in Unicode as 🗑. */
    object Wastebasket : UnicodeEmoji("\ud83d\uddd1")

    /** Unicode emoji with a Discord shortcode of `:oil:`. Represented in Unicode as 🛢. */
    object Oil : UnicodeEmoji("\ud83d\udee2")

    /** Unicode emoji with a Discord shortcode of `:money_with_wings:`. Represented in Unicode as 💸. */
    object MoneyWithWings : UnicodeEmoji("\ud83d\udcb8")

    /** Unicode emoji with a Discord shortcode of `:dollar:`. Represented in Unicode as 💵. */
    object Dollar : UnicodeEmoji("\ud83d\udcb5")

    /** Unicode emoji with a Discord shortcode of `:yen:`. Represented in Unicode as 💴. */
    object Yen : UnicodeEmoji("\ud83d\udcb4")

    /** Unicode emoji with a Discord shortcode of `:euro:`. Represented in Unicode as 💶. */
    object Euro : UnicodeEmoji("\ud83d\udcb6")

    /** Unicode emoji with a Discord shortcode of `:pound:`. Represented in Unicode as 💷. */
    object Pound : UnicodeEmoji("\ud83d\udcb7")

    /** Unicode emoji with a Discord shortcode of `:moneybag:`. Represented in Unicode as 💰. */
    object Moneybag : UnicodeEmoji("\ud83d\udcb0")

    /** Unicode emoji with a Discord shortcode of `:credit_card:`. Represented in Unicode as 💳. */
    object CreditCard : UnicodeEmoji("\ud83d\udcb3")

    /** Unicode emoji with a Discord shortcode of `:gem:`. Represented in Unicode as 💎. */
    object Gem : UnicodeEmoji("\ud83d\udc8e")

    /** Unicode emoji with a Discord shortcode of `:scales:`. Represented in Unicode as ⚖. */
    object Scales : UnicodeEmoji("\u2696")

    /** Unicode emoji with a Discord shortcode of `:wrench:`. Represented in Unicode as 🔧. */
    object Wrench : UnicodeEmoji("\ud83d\udd27")

    /** Unicode emoji with a Discord shortcode of `:hammer:`. Represented in Unicode as 🔨. */
    object Hammer : UnicodeEmoji("\ud83d\udd28")

    /** Unicode emoji with a Discord shortcode of `:hammer_pick:`. Represented in Unicode as ⚒. */
    object HammerPick : UnicodeEmoji("\u2692")

    /** Unicode emoji with a Discord shortcode of `:tools:`. Represented in Unicode as 🛠. */
    object Tools : UnicodeEmoji("\ud83d\udee0")

    /** Unicode emoji with a Discord shortcode of `:pick:`. Represented in Unicode as ⛏. */
    object Pick : UnicodeEmoji("\u26cf")

    /** Unicode emoji with a Discord shortcode of `:nut_and_bolt:`. Represented in Unicode as 🔩. */
    object NutAndBolt : UnicodeEmoji("\ud83d\udd29")

    /** Unicode emoji with a Discord shortcode of `:gear:`. Represented in Unicode as ⚙. */
    object Gear : UnicodeEmoji("\u2699")

    /** Unicode emoji with a Discord shortcode of `:chains:`. Represented in Unicode as ⛓. */
    object Chains : UnicodeEmoji("\u26d3")

    /** Unicode emoji with a Discord shortcode of `:gun:`. Represented in Unicode as 🔫. */
    object Gun : UnicodeEmoji("\ud83d\udd2b")

    /** Unicode emoji with a Discord shortcode of `:bomb:`. Represented in Unicode as 💣. */
    object Bomb : UnicodeEmoji("\ud83d\udca3")

    /** Unicode emoji with a Discord shortcode of `:knife:`. Represented in Unicode as 🔪. */
    object Knife : UnicodeEmoji("\ud83d\udd2a")

    /** Unicode emoji with a Discord shortcode of `:dagger:`. Represented in Unicode as 🗡. */
    object Dagger : UnicodeEmoji("\ud83d\udde1")

    /** Unicode emoji with a Discord shortcode of `:crossed_swords:`. Represented in Unicode as ⚔. */
    object CrossedSwords : UnicodeEmoji("\u2694")

    /** Unicode emoji with a Discord shortcode of `:shield:`. Represented in Unicode as 🛡. */
    object Shield : UnicodeEmoji("\ud83d\udee1")

    /** Unicode emoji with a Discord shortcode of `:smoking:`. Represented in Unicode as 🚬. */
    object Smoking : UnicodeEmoji("\ud83d\udeac")

    /** Unicode emoji with a Discord shortcode of `:skull_crossbones:`. Represented in Unicode as ☠. */
    object SkullCrossbones : UnicodeEmoji("\u2620")

    /** Unicode emoji with a Discord shortcode of `:coffin:`. Represented in Unicode as ⚰. */
    object Coffin : UnicodeEmoji("\u26b0")

    /** Unicode emoji with a Discord shortcode of `:urn:`. Represented in Unicode as ⚱. */
    object Urn : UnicodeEmoji("\u26b1")

    /** Unicode emoji with a Discord shortcode of `:amphora:`. Represented in Unicode as 🏺. */
    object Amphora : UnicodeEmoji("\ud83c\udffa")

    /** Unicode emoji with a Discord shortcode of `:crystal_ball:`. Represented in Unicode as 🔮. */
    object CrystalBall : UnicodeEmoji("\ud83d\udd2e")

    /** Unicode emoji with a Discord shortcode of `:prayer_beads:`. Represented in Unicode as 📿. */
    object PrayerBeads : UnicodeEmoji("\ud83d\udcff")

    /** Unicode emoji with a Discord shortcode of `:barber:`. Represented in Unicode as 💈. */
    object Barber : UnicodeEmoji("\ud83d\udc88")

    /** Unicode emoji with a Discord shortcode of `:alembic:`. Represented in Unicode as ⚗. */
    object Alembic : UnicodeEmoji("\u2697")

    /** Unicode emoji with a Discord shortcode of `:telescope:`. Represented in Unicode as 🔭. */
    object Telescope : UnicodeEmoji("\ud83d\udd2d")

    /** Unicode emoji with a Discord shortcode of `:microscope:`. Represented in Unicode as 🔬. */
    object Microscope : UnicodeEmoji("\ud83d\udd2c")

    /** Unicode emoji with a Discord shortcode of `:hole:`. Represented in Unicode as 🕳. */
    object Hole : UnicodeEmoji("\ud83d\udd73")

    /** Unicode emoji with a Discord shortcode of `:pill:`. Represented in Unicode as 💊. */
    object Pill : UnicodeEmoji("\ud83d\udc8a")

    /** Unicode emoji with a Discord shortcode of `:syringe:`. Represented in Unicode as 💉. */
    object Syringe : UnicodeEmoji("\ud83d\udc89")

    /** Unicode emoji with a Discord shortcode of `:thermometer:`. Represented in Unicode as 🌡. */
    object Thermometer : UnicodeEmoji("\ud83c\udf21")

    /** Unicode emoji with a Discord shortcode of `:label:`. Represented in Unicode as 🏷. */
    object Label : UnicodeEmoji("\ud83c\udff7")

    /** Unicode emoji with a Discord shortcode of `:bookmark:`. Represented in Unicode as 🔖. */
    object Bookmark : UnicodeEmoji("\ud83d\udd16")

    /** Unicode emoji with a Discord shortcode of `:toilet:`. Represented in Unicode as 🚽. */
    object Toilet : UnicodeEmoji("\ud83d\udebd")

    /** Unicode emoji with a Discord shortcode of `:shower:`. Represented in Unicode as 🚿. */
    object Shower : UnicodeEmoji("\ud83d\udebf")

    /** Unicode emoji with a Discord shortcode of `:bathtub:`. Represented in Unicode as 🛁. */
    object Bathtub : UnicodeEmoji("\ud83d\udec1")

    /** Unicode emoji with a Discord shortcode of `:key:`. Represented in Unicode as 🔑. */
    object Key : UnicodeEmoji("\ud83d\udd11")

    /** Unicode emoji with a Discord shortcode of `:key2:`. Represented in Unicode as 🗝. */
    object Key2 : UnicodeEmoji("\ud83d\udddd")

    /** Unicode emoji with a Discord shortcode of `:couch:`. Represented in Unicode as 🛋. */
    object Couch : UnicodeEmoji("\ud83d\udecb")

    /** Unicode emoji with a Discord shortcode of `:sleeping_accommodation:`. Represented in Unicode as 🛌. */
    object SleepingAccommodation : UnicodeEmoji("\ud83d\udecc")

    /** Unicode emoji with a Discord shortcode of `:bed:`. Represented in Unicode as 🛏. */
    object Bed : UnicodeEmoji("\ud83d\udecf")

    /** Unicode emoji with a Discord shortcode of `:door:`. Represented in Unicode as 🚪. */
    object Door : UnicodeEmoji("\ud83d\udeaa")

    /** Unicode emoji with a Discord shortcode of `:bellhop:`. Represented in Unicode as 🛎. */
    object Bellhop : UnicodeEmoji("\ud83d\udece")

    /** Unicode emoji with a Discord shortcode of `:frame_photo:`. Represented in Unicode as 🖼. */
    object FramePhoto : UnicodeEmoji("\ud83d\uddbc")

    /** Unicode emoji with a Discord shortcode of `:map:`. Represented in Unicode as 🗺. */
    object Map : UnicodeEmoji("\ud83d\uddfa")

    /** Unicode emoji with a Discord shortcode of `:beach_umbrella:`. Represented in Unicode as ⛱. */
    object BeachUmbrella : UnicodeEmoji("\u26f1")

    /** Unicode emoji with a Discord shortcode of `:moyai:`. Represented in Unicode as 🗿. */
    object Moyai : UnicodeEmoji("\ud83d\uddff")

    /** Unicode emoji with a Discord shortcode of `:shopping_bags:`. Represented in Unicode as 🛍. */
    object ShoppingBags : UnicodeEmoji("\ud83d\udecd")

    /** Unicode emoji with a Discord shortcode of `:balloon:`. Represented in Unicode as 🎈. */
    object Balloon : UnicodeEmoji("\ud83c\udf88")

    /** Unicode emoji with a Discord shortcode of `:flags:`. Represented in Unicode as 🎏. */
    object Flags : UnicodeEmoji("\ud83c\udf8f")

    /** Unicode emoji with a Discord shortcode of `:ribbon:`. Represented in Unicode as 🎀. */
    object Ribbon : UnicodeEmoji("\ud83c\udf80")

    /** Unicode emoji with a Discord shortcode of `:gift:`. Represented in Unicode as 🎁. */
    object Gift : UnicodeEmoji("\ud83c\udf81")

    /** Unicode emoji with a Discord shortcode of `:confetti_ball:`. Represented in Unicode as 🎊. */
    object ConfettiBall : UnicodeEmoji("\ud83c\udf8a")

    /** Unicode emoji with a Discord shortcode of `:tada:`. Represented in Unicode as 🎉. */
    object Tada : UnicodeEmoji("\ud83c\udf89")

    /** Unicode emoji with a Discord shortcode of `:dolls:`. Represented in Unicode as 🎎. */
    object Dolls : UnicodeEmoji("\ud83c\udf8e")

    /** Unicode emoji with a Discord shortcode of `:wind_chime:`. Represented in Unicode as 🎐. */
    object WindChime : UnicodeEmoji("\ud83c\udf90")

    /** Unicode emoji with a Discord shortcode of `:crossed_flags:`. Represented in Unicode as 🎌. */
    object CrossedFlags : UnicodeEmoji("\ud83c\udf8c")

    /** Unicode emoji with a Discord shortcode of `:lantern:`. Represented in Unicode as 🏮. */
    object Lantern : UnicodeEmoji("\ud83c\udfee")

    /** Unicode emoji with a Discord shortcode of `:envelope:`. Represented in Unicode as ✉. */
    object Envelope : UnicodeEmoji("\u2709")

    /** Unicode emoji with a Discord shortcode of `:email:`. Represented in Unicode as 📧. */
    object Email : UnicodeEmoji("\ud83d\udce7")

    /** Unicode emoji with a Discord shortcode of `:envelope_with_arrow:`. Represented in Unicode as 📩. */
    object EnvelopeWithArrow : UnicodeEmoji("\ud83d\udce9")

    /** Unicode emoji with a Discord shortcode of `:incoming_envelope:`. Represented in Unicode as 📨. */
    object IncomingEnvelope : UnicodeEmoji("\ud83d\udce8")

    /** Unicode emoji with a Discord shortcode of `:love_letter:`. Represented in Unicode as 💌. */
    object LoveLetter : UnicodeEmoji("\ud83d\udc8c")

    /** Unicode emoji with a Discord shortcode of `:postbox:`. Represented in Unicode as 📮. */
    object Postbox : UnicodeEmoji("\ud83d\udcee")

    /** Unicode emoji with a Discord shortcode of `:mailbox_closed:`. Represented in Unicode as 📪. */
    object MailboxClosed : UnicodeEmoji("\ud83d\udcea")

    /** Unicode emoji with a Discord shortcode of `:mailbox:`. Represented in Unicode as 📫. */
    object Mailbox : UnicodeEmoji("\ud83d\udceb")

    /** Unicode emoji with a Discord shortcode of `:mailbox_with_mail:`. Represented in Unicode as 📬. */
    object MailboxWithMail : UnicodeEmoji("\ud83d\udcec")

    /** Unicode emoji with a Discord shortcode of `:mailbox_with_no_mail:`. Represented in Unicode as 📭. */
    object MailboxWithNoMail : UnicodeEmoji("\ud83d\udced")

    /** Unicode emoji with a Discord shortcode of `:package:`. Represented in Unicode as 📦. */
    object Package : UnicodeEmoji("\ud83d\udce6")

    /** Unicode emoji with a Discord shortcode of `:postal_horn:`. Represented in Unicode as 📯. */
    object PostalHorn : UnicodeEmoji("\ud83d\udcef")

    /** Unicode emoji with a Discord shortcode of `:inbox_tray:`. Represented in Unicode as 📥. */
    object InboxTray : UnicodeEmoji("\ud83d\udce5")

    /** Unicode emoji with a Discord shortcode of `:outbox_tray:`. Represented in Unicode as 📤. */
    object OutboxTray : UnicodeEmoji("\ud83d\udce4")

    /** Unicode emoji with a Discord shortcode of `:scroll:`. Represented in Unicode as 📜. */
    object Scroll : UnicodeEmoji("\ud83d\udcdc")

    /** Unicode emoji with a Discord shortcode of `:page_with_curl:`. Represented in Unicode as 📃. */
    object PageWithCurl : UnicodeEmoji("\ud83d\udcc3")

    /** Unicode emoji with a Discord shortcode of `:bookmark_tabs:`. Represented in Unicode as 📑. */
    object BookmarkTabs : UnicodeEmoji("\ud83d\udcd1")

    /** Unicode emoji with a Discord shortcode of `:bar_chart:`. Represented in Unicode as 📊. */
    object BarChart : UnicodeEmoji("\ud83d\udcca")

    /** Unicode emoji with a Discord shortcode of `:chart_with_upwards_trend:`. Represented in Unicode as 📈. */
    object ChartWithUpwardsTrend : UnicodeEmoji("\ud83d\udcc8")

    /** Unicode emoji with a Discord shortcode of `:chart_with_downwards_trend:`. Represented in Unicode as 📉. */
    object ChartWithDownwardsTrend : UnicodeEmoji("\ud83d\udcc9")

    /** Unicode emoji with a Discord shortcode of `:page_facing_up:`. Represented in Unicode as 📄. */
    object PageFacingUp : UnicodeEmoji("\ud83d\udcc4")

    /** Unicode emoji with a Discord shortcode of `:date:`. Represented in Unicode as 📅. */
    object Date : UnicodeEmoji("\ud83d\udcc5")

    /** Unicode emoji with a Discord shortcode of `:calendar:`. Represented in Unicode as 📆. */
    object Calendar : UnicodeEmoji("\ud83d\udcc6")

    /** Unicode emoji with a Discord shortcode of `:calendar_spiral:`. Represented in Unicode as 🗓. */
    object CalendarSpiral : UnicodeEmoji("\ud83d\uddd3")

    /** Unicode emoji with a Discord shortcode of `:card_index:`. Represented in Unicode as 📇. */
    object CardIndex : UnicodeEmoji("\ud83d\udcc7")

    /** Unicode emoji with a Discord shortcode of `:card_box:`. Represented in Unicode as 🗃. */
    object CardBox : UnicodeEmoji("\ud83d\uddc3")

    /** Unicode emoji with a Discord shortcode of `:ballot_box:`. Represented in Unicode as 🗳. */
    object BallotBox : UnicodeEmoji("\ud83d\uddf3")

    /** Unicode emoji with a Discord shortcode of `:file_cabinet:`. Represented in Unicode as 🗄. */
    object FileCabinet : UnicodeEmoji("\ud83d\uddc4")

    /** Unicode emoji with a Discord shortcode of `:clipboard:`. Represented in Unicode as 📋. */
    object Clipboard : UnicodeEmoji("\ud83d\udccb")

    /** Unicode emoji with a Discord shortcode of `:notepad_spiral:`. Represented in Unicode as 🗒. */
    object NotepadSpiral : UnicodeEmoji("\ud83d\uddd2")

    /** Unicode emoji with a Discord shortcode of `:file_folder:`. Represented in Unicode as 📁. */
    object FileFolder : UnicodeEmoji("\ud83d\udcc1")

    /** Unicode emoji with a Discord shortcode of `:open_file_folder:`. Represented in Unicode as 📂. */
    object OpenFileFolder : UnicodeEmoji("\ud83d\udcc2")

    /** Unicode emoji with a Discord shortcode of `:dividers:`. Represented in Unicode as 🗂. */
    object Dividers : UnicodeEmoji("\ud83d\uddc2")

    /** Unicode emoji with a Discord shortcode of `:newspaper2:`. Represented in Unicode as 🗞. */
    object Newspaper2 : UnicodeEmoji("\ud83d\uddde")

    /** Unicode emoji with a Discord shortcode of `:newspaper:`. Represented in Unicode as 📰. */
    object Newspaper : UnicodeEmoji("\ud83d\udcf0")

    /** Unicode emoji with a Discord shortcode of `:notebook:`. Represented in Unicode as 📓. */
    object Notebook : UnicodeEmoji("\ud83d\udcd3")

    /** Unicode emoji with a Discord shortcode of `:closed_book:`. Represented in Unicode as 📕. */
    object ClosedBook : UnicodeEmoji("\ud83d\udcd5")

    /** Unicode emoji with a Discord shortcode of `:green_book:`. Represented in Unicode as 📗. */
    object GreenBook : UnicodeEmoji("\ud83d\udcd7")

    /** Unicode emoji with a Discord shortcode of `:blue_book:`. Represented in Unicode as 📘. */
    object BlueBook : UnicodeEmoji("\ud83d\udcd8")

    /** Unicode emoji with a Discord shortcode of `:orange_book:`. Represented in Unicode as 📙. */
    object OrangeBook : UnicodeEmoji("\ud83d\udcd9")

    /** Unicode emoji with a Discord shortcode of `:notebook_with_decorative_cover:`. Represented in Unicode as 📔. */
    object NotebookWithDecorativeCover : UnicodeEmoji("\ud83d\udcd4")

    /** Unicode emoji with a Discord shortcode of `:ledger:`. Represented in Unicode as 📒. */
    object Ledger : UnicodeEmoji("\ud83d\udcd2")

    /** Unicode emoji with a Discord shortcode of `:books:`. Represented in Unicode as 📚. */
    object Books : UnicodeEmoji("\ud83d\udcda")

    /** Unicode emoji with a Discord shortcode of `:book:`. Represented in Unicode as 📖. */
    object Book : UnicodeEmoji("\ud83d\udcd6")

    /** Unicode emoji with a Discord shortcode of `:link:`. Represented in Unicode as 🔗. */
    object Link : UnicodeEmoji("\ud83d\udd17")

    /** Unicode emoji with a Discord shortcode of `:paperclip:`. Represented in Unicode as 📎. */
    object Paperclip : UnicodeEmoji("\ud83d\udcce")

    /** Unicode emoji with a Discord shortcode of `:paperclips:`. Represented in Unicode as 🖇. */
    object Paperclips : UnicodeEmoji("\ud83d\udd87")

    /** Unicode emoji with a Discord shortcode of `:scissors:`. Represented in Unicode as ✂. */
    object Scissors : UnicodeEmoji("\u2702")

    /** Unicode emoji with a Discord shortcode of `:triangular_ruler:`. Represented in Unicode as 📐. */
    object TriangularRuler : UnicodeEmoji("\ud83d\udcd0")

    /** Unicode emoji with a Discord shortcode of `:straight_ruler:`. Represented in Unicode as 📏. */
    object StraightRuler : UnicodeEmoji("\ud83d\udccf")

    /** Unicode emoji with a Discord shortcode of `:pushpin:`. Represented in Unicode as 📌. */
    object Pushpin : UnicodeEmoji("\ud83d\udccc")

    /** Unicode emoji with a Discord shortcode of `:round_pushpin:`. Represented in Unicode as 📍. */
    object RoundPushpin : UnicodeEmoji("\ud83d\udccd")

    /** Unicode emoji with a Discord shortcode of `:triangular_flag_on_post:`. Represented in Unicode as 🚩. */
    object TriangularFlagOnPost : UnicodeEmoji("\ud83d\udea9")

    /** Unicode emoji with a Discord shortcode of `:flag_white:`. Represented in Unicode as 🏳. */
    object FlagWhite : UnicodeEmoji("\ud83c\udff3")

    /** Unicode emoji with a Discord shortcode of `:flag_black:`. Represented in Unicode as 🏴. */
    object FlagBlack : UnicodeEmoji("\ud83c\udff4")

    /** Unicode emoji with a Discord shortcode of `:closed_lock_with_key:`. Represented in Unicode as 🔐. */
    object ClosedLockWithKey : UnicodeEmoji("\ud83d\udd10")

    /** Unicode emoji with a Discord shortcode of `:lock:`. Represented in Unicode as 🔒. */
    object Lock : UnicodeEmoji("\ud83d\udd12")

    /** Unicode emoji with a Discord shortcode of `:unlock:`. Represented in Unicode as 🔓. */
    object Unlock : UnicodeEmoji("\ud83d\udd13")

    /** Unicode emoji with a Discord shortcode of `:lock_with_ink_pen:`. Represented in Unicode as 🔏. */
    object LockWithInkPen : UnicodeEmoji("\ud83d\udd0f")

    /** Unicode emoji with a Discord shortcode of `:pen_ballpoint:`. Represented in Unicode as 🖊. */
    object PenBallpoint : UnicodeEmoji("\ud83d\udd8a")

    /** Unicode emoji with a Discord shortcode of `:pen_fountain:`. Represented in Unicode as 🖋. */
    object PenFountain : UnicodeEmoji("\ud83d\udd8b")

    /** Unicode emoji with a Discord shortcode of `:black_nib:`. Represented in Unicode as ✒. */
    object BlackNib : UnicodeEmoji("\u2712")

    /** Unicode emoji with a Discord shortcode of `:memo:`. Represented in Unicode as 📝. */
    object Memo : UnicodeEmoji("\ud83d\udcdd")

    /** Unicode emoji with a Discord shortcode of `:pencil2:`. Represented in Unicode as ✏. */
    object Pencil2 : UnicodeEmoji("\u270f")

    /** Unicode emoji with a Discord shortcode of `:crayon:`. Represented in Unicode as 🖍. */
    object Crayon : UnicodeEmoji("\ud83d\udd8d")

    /** Unicode emoji with a Discord shortcode of `:paintbrush:`. Represented in Unicode as 🖌. */
    object Paintbrush : UnicodeEmoji("\ud83d\udd8c")

    /** Unicode emoji with a Discord shortcode of `:mag:`. Represented in Unicode as 🔍. */
    object Mag : UnicodeEmoji("\ud83d\udd0d")

    /** Unicode emoji with a Discord shortcode of `:mag_right:`. Represented in Unicode as 🔎. */
    object MagRight : UnicodeEmoji("\ud83d\udd0e")

    /** Unicode emoji with a Discord shortcode of `:shopping_cart:`. Represented in Unicode as 🛒. */
    object ShoppingCart : UnicodeEmoji("\ud83d\uded2")

    // Symbols

    /** Unicode emoji with a Discord shortcode of `:one_hundred:`. Represented in Unicode as 💯. */
    object OneHundred : UnicodeEmoji("\ud83d\udcaf")

    /** Unicode emoji with a Discord shortcode of `:one_two_three_four:`. Represented in Unicode as 🔢. */
    object OneTwoThreeFour : UnicodeEmoji("\ud83d\udd22")

    /** Unicode emoji with a Discord shortcode of `:heart:`. Represented in Unicode as ❤. */
    object Heart : UnicodeEmoji("\u2764")

    /** Unicode emoji with a Discord shortcode of `:yellow_heart:`. Represented in Unicode as 💛. */
    object YellowHeart : UnicodeEmoji("\ud83d\udc9b")

    /** Unicode emoji with a Discord shortcode of `:green_heart:`. Represented in Unicode as 💚. */
    object GreenHeart : UnicodeEmoji("\ud83d\udc9a")

    /** Unicode emoji with a Discord shortcode of `:blue_heart:`. Represented in Unicode as 💙. */
    object BlueHeart : UnicodeEmoji("\ud83d\udc99")

    /** Unicode emoji with a Discord shortcode of `:purple_heart:`. Represented in Unicode as 💜. */
    object PurpleHeart : UnicodeEmoji("\ud83d\udc9c")

    /** Unicode emoji with a Discord shortcode of `:broken_heart:`. Represented in Unicode as 💔. */
    object BrokenHeart : UnicodeEmoji("\ud83d\udc94")

    /** Unicode emoji with a Discord shortcode of `:heart_exclamation:`. Represented in Unicode as ❣. */
    object HeartExclamation : UnicodeEmoji("\u2763")

    /** Unicode emoji with a Discord shortcode of `:two_hearts:`. Represented in Unicode as 💕. */
    object TwoHearts : UnicodeEmoji("\ud83d\udc95")

    /** Unicode emoji with a Discord shortcode of `:revolving_hearts:`. Represented in Unicode as 💞. */
    object RevolvingHearts : UnicodeEmoji("\ud83d\udc9e")

    /** Unicode emoji with a Discord shortcode of `:heartbeat:`. Represented in Unicode as 💓. */
    object Heartbeat : UnicodeEmoji("\ud83d\udc93")

    /** Unicode emoji with a Discord shortcode of `:heartpulse:`. Represented in Unicode as 💗. */
    object Heartpulse : UnicodeEmoji("\ud83d\udc97")

    /** Unicode emoji with a Discord shortcode of `:sparkling_heart:`. Represented in Unicode as 💖. */
    object SparklingHeart : UnicodeEmoji("\ud83d\udc96")

    /** Unicode emoji with a Discord shortcode of `:cupid:`. Represented in Unicode as 💘. */
    object Cupid : UnicodeEmoji("\ud83d\udc98")

    /** Unicode emoji with a Discord shortcode of `:gift_heart:`. Represented in Unicode as 💝. */
    object GiftHeart : UnicodeEmoji("\ud83d\udc9d")

    /** Unicode emoji with a Discord shortcode of `:heart_decoration:`. Represented in Unicode as 💟. */
    object HeartDecoration : UnicodeEmoji("\ud83d\udc9f")

    /** Unicode emoji with a Discord shortcode of `:peace:`. Represented in Unicode as ☮. */
    object Peace : UnicodeEmoji("\u262e")

    /** Unicode emoji with a Discord shortcode of `:cross:`. Represented in Unicode as ✝. */
    object Cross : UnicodeEmoji("\u271d")

    /** Unicode emoji with a Discord shortcode of `:star_and_crescent:`. Represented in Unicode as ☪. */
    object StarAndCrescent : UnicodeEmoji("\u262a")

    /** Unicode emoji with a Discord shortcode of `:om_symbol:`. Represented in Unicode as 🕉. */
    object OmSymbol : UnicodeEmoji("\ud83d\udd49")

    /** Unicode emoji with a Discord shortcode of `:wheel_of_dharma:`. Represented in Unicode as ☸. */
    object WheelOfDharma : UnicodeEmoji("\u2638")

    /** Unicode emoji with a Discord shortcode of `:star_of_david:`. Represented in Unicode as ✡. */
    object StarOfDavid : UnicodeEmoji("\u2721")

    /** Unicode emoji with a Discord shortcode of `:six_pointed_star:`. Represented in Unicode as 🔯. */
    object SixPointedStar : UnicodeEmoji("\ud83d\udd2f")

    /** Unicode emoji with a Discord shortcode of `:menorah:`. Represented in Unicode as 🕎. */
    object Menorah : UnicodeEmoji("\ud83d\udd4e")

    /** Unicode emoji with a Discord shortcode of `:yin_yang:`. Represented in Unicode as ☯. */
    object YinYang : UnicodeEmoji("\u262f")

    /** Unicode emoji with a Discord shortcode of `:orthodox_cross:`. Represented in Unicode as ☦. */
    object OrthodoxCross : UnicodeEmoji("\u2626")

    /** Unicode emoji with a Discord shortcode of `:place_of_worship:`. Represented in Unicode as 🛐. */
    object PlaceOfWorship : UnicodeEmoji("\ud83d\uded0")

    /** Unicode emoji with a Discord shortcode of `:ophiuchus:`. Represented in Unicode as ⛎. */
    object Ophiuchus : UnicodeEmoji("\u26ce")

    /** Unicode emoji with a Discord shortcode of `:aries:`. Represented in Unicode as ♈. */
    object Aries : UnicodeEmoji("\u2648")

    /** Unicode emoji with a Discord shortcode of `:taurus:`. Represented in Unicode as ♉. */
    object Taurus : UnicodeEmoji("\u2649")

    /** Unicode emoji with a Discord shortcode of `:gemini:`. Represented in Unicode as ♊. */
    object Gemini : UnicodeEmoji("\u264a")

    /** Unicode emoji with a Discord shortcode of `:cancer:`. Represented in Unicode as ♋. */
    object Cancer : UnicodeEmoji("\u264b")

    /** Unicode emoji with a Discord shortcode of `:leo:`. Represented in Unicode as ♌. */
    object Leo : UnicodeEmoji("\u264c")

    /** Unicode emoji with a Discord shortcode of `:virgo:`. Represented in Unicode as ♍. */
    object Virgo : UnicodeEmoji("\u264d")

    /** Unicode emoji with a Discord shortcode of `:libra:`. Represented in Unicode as ♎. */
    object Libra : UnicodeEmoji("\u264e")

    /** Unicode emoji with a Discord shortcode of `:scorpius:`. Represented in Unicode as ♏. */
    object Scorpius : UnicodeEmoji("\u264f")

    /** Unicode emoji with a Discord shortcode of `:sagittarius:`. Represented in Unicode as ♐. */
    object Sagittarius : UnicodeEmoji("\u2650")

    /** Unicode emoji with a Discord shortcode of `:capricorn:`. Represented in Unicode as ♑. */
    object Capricorn : UnicodeEmoji("\u2651")

    /** Unicode emoji with a Discord shortcode of `:aquarius:`. Represented in Unicode as ♒. */
    object Aquarius : UnicodeEmoji("\u2652")

    /** Unicode emoji with a Discord shortcode of `:pisces:`. Represented in Unicode as ♓. */
    object Pisces : UnicodeEmoji("\u2653")

    /** Unicode emoji with a Discord shortcode of `:id:`. Represented in Unicode as 🆔. */
    object ID : UnicodeEmoji("\ud83c\udd94")

    /** Unicode emoji with a Discord shortcode of `:atom:`. Represented in Unicode as ⚛. */
    object Atom : UnicodeEmoji("\u269b")

    /** Unicode emoji with a Discord shortcode of `:u7a7a:`. Represented in Unicode as 🈳. */
    object U7a7a : UnicodeEmoji("\ud83c\ude33")

    /** Unicode emoji with a Discord shortcode of `:u5272:`. Represented in Unicode as 🈹. */
    object U5272 : UnicodeEmoji("\ud83c\ude39")

    /** Unicode emoji with a Discord shortcode of `:radioactive:`. Represented in Unicode as ☢. */
    object Radioactive : UnicodeEmoji("\u2622")

    /** Unicode emoji with a Discord shortcode of `:biohazard:`. Represented in Unicode as ☣. */
    object Biohazard : UnicodeEmoji("\u2623")

    /** Unicode emoji with a Discord shortcode of `:mobile_phone_off:`. Represented in Unicode as 📴. */
    object MobilePhoneOff : UnicodeEmoji("\ud83d\udcf4")

    /** Unicode emoji with a Discord shortcode of `:vibration_mode:`. Represented in Unicode as 📳. */
    object VibrationMode : UnicodeEmoji("\ud83d\udcf3")

    /** Unicode emoji with a Discord shortcode of `:u6709:`. Represented in Unicode as 🈶. */
    object U6709 : UnicodeEmoji("\ud83c\ude36")

    /** Unicode emoji with a Discord shortcode of `:u7121:`. Represented in Unicode as 🈚. */
    object U7121 : UnicodeEmoji("\ud83c\ude1a")

    /** Unicode emoji with a Discord shortcode of `:u7533:`. Represented in Unicode as 🈸. */
    object U7533 : UnicodeEmoji("\ud83c\ude38")

    /** Unicode emoji with a Discord shortcode of `:u55b6:`. Represented in Unicode as 🈺. */
    object U55b6 : UnicodeEmoji("\ud83c\ude3a")

    /** Unicode emoji with a Discord shortcode of `:u6708:`. Represented in Unicode as 🈷. */
    object U6708 : UnicodeEmoji("\ud83c\ude37")

    /** Unicode emoji with a Discord shortcode of `:eight_pointed_black_star:`. Represented in Unicode as ✴. */
    object EightPointedBlackStar : UnicodeEmoji("\u2734")

    /** Unicode emoji with a Discord shortcode of `:vs:`. Represented in Unicode as 🆚. */
    object VS : UnicodeEmoji("\ud83c\udd9a")

    /** Unicode emoji with a Discord shortcode of `:accept:`. Represented in Unicode as 🉑. */
    object Accept : UnicodeEmoji("\ud83c\ude51")

    /** Unicode emoji with a Discord shortcode of `:white_flower:`. Represented in Unicode as 💮. */
    object WhiteFlower : UnicodeEmoji("\ud83d\udcae")

    /** Unicode emoji with a Discord shortcode of `:ideograph_advantage:`. Represented in Unicode as 🉐. */
    object IdeographAdvantage : UnicodeEmoji("\ud83c\ude50")

    /** Unicode emoji with a Discord shortcode of `:secret:`. Represented in Unicode as ㊙. */
    object Secret : UnicodeEmoji("\u3299")

    /** Unicode emoji with a Discord shortcode of `:congratulations:`. Represented in Unicode as ㊗. */
    object Congratulations : UnicodeEmoji("\u3297")

    /** Unicode emoji with a Discord shortcode of `:u5408:`. Represented in Unicode as 🈴. */
    object U5408 : UnicodeEmoji("\ud83c\ude34")

    /** Unicode emoji with a Discord shortcode of `:u6e80:`. Represented in Unicode as 🈵. */
    object U6e80 : UnicodeEmoji("\ud83c\ude35")

    /** Unicode emoji with a Discord shortcode of `:u7981:`. Represented in Unicode as 🈲. */
    object U7981 : UnicodeEmoji("\ud83c\ude32")

    /** Unicode emoji with a Discord shortcode of `:a:`. Represented in Unicode as 🅰. */
    object A : UnicodeEmoji("\ud83c\udd70")

    /** Unicode emoji with a Discord shortcode of `:b:`. Represented in Unicode as 🅱. */
    object B : UnicodeEmoji("\ud83c\udd71")

    /** Unicode emoji with a Discord shortcode of `:ab:`. Represented in Unicode as 🆎. */
    object AB : UnicodeEmoji("\ud83c\udd8e")

    /** Unicode emoji with a Discord shortcode of `:cl:`. Represented in Unicode as 🆑. */
    object CL : UnicodeEmoji("\ud83c\udd91")

    /** Unicode emoji with a Discord shortcode of `:o2:`. Represented in Unicode as 🅾. */
    object O2 : UnicodeEmoji("\ud83c\udd7e")

    /** Unicode emoji with a Discord shortcode of `:sos:`. Represented in Unicode as 🆘. */
    object Sos : UnicodeEmoji("\ud83c\udd98")

    /** Unicode emoji with a Discord shortcode of `:no_entry:`. Represented in Unicode as ⛔. */
    object NoEntry : UnicodeEmoji("\u26d4")

    /** Unicode emoji with a Discord shortcode of `:name_badge:`. Represented in Unicode as 📛. */
    object NameBadge : UnicodeEmoji("\ud83d\udcdb")

    /** Unicode emoji with a Discord shortcode of `:no_entry_sign:`. Represented in Unicode as 🚫. */
    object NoEntrySign : UnicodeEmoji("\ud83d\udeab")

    /** A red 'X' */
    /** Unicode emoji with a Discord shortcode of `:x:`. Represented in Unicode as ❌. */
    object X : UnicodeEmoji("\u274c")

    /** Unicode emoji with a Discord shortcode of `:o:`. Represented in Unicode as ⭕. */
    object O : UnicodeEmoji("\u2b55")

    /** Unicode emoji with a Discord shortcode of `:anger:`. Represented in Unicode as 💢. */
    object Anger : UnicodeEmoji("\ud83d\udca2")

    /** Unicode emoji with a Discord shortcode of `:hotsprings:`. Represented in Unicode as ♨. */
    object Hotsprings : UnicodeEmoji("\u2668")

    /** Unicode emoji with a Discord shortcode of `:no_pedestrians:`. Represented in Unicode as 🚷. */
    object NoPedestrians : UnicodeEmoji("\ud83d\udeb7")

    /** Unicode emoji with a Discord shortcode of `:do_not_litter:`. Represented in Unicode as 🚯. */
    object DoNotLitter : UnicodeEmoji("\ud83d\udeaf")

    /** Unicode emoji with a Discord shortcode of `:no_bicycles:`. Represented in Unicode as 🚳. */
    object NoBicycles : UnicodeEmoji("\ud83d\udeb3")

    /** Unicode emoji with a Discord shortcode of `:non_potable_water:`. Represented in Unicode as 🚱. */
    object NonPotableWater : UnicodeEmoji("\ud83d\udeb1")

    /** Unicode emoji with a Discord shortcode of `:underage:`. Represented in Unicode as 🔞. */
    object Underage : UnicodeEmoji("\ud83d\udd1e")

    /** Unicode emoji with a Discord shortcode of `:no_mobile_phones:`. Represented in Unicode as 📵. */
    object NoMobilePhones : UnicodeEmoji("\ud83d\udcf5")

    /** Unicode emoji with a Discord shortcode of `:exclamation:`. Represented in Unicode as ❗. */
    object Exclamation : UnicodeEmoji("\u2757")

    /** Unicode emoji with a Discord shortcode of `:grey_exclamation:`. Represented in Unicode as ❕. */
    object GreyExclamation : UnicodeEmoji("\u2755")

    /** Unicode emoji with a Discord shortcode of `:question:`. Represented in Unicode as ❓. */
    object Question : UnicodeEmoji("\u2753")

    /** Unicode emoji with a Discord shortcode of `:grey_question:`. Represented in Unicode as ❔. */
    object GreyQuestion : UnicodeEmoji("\u2754")

    /** Unicode emoji with a Discord shortcode of `:bangbang:`. Represented in Unicode as ‼. */
    object Bangbang : UnicodeEmoji("\u203c")

    /** Unicode emoji with a Discord shortcode of `:interrobang:`. Represented in Unicode as ⁉. */
    object Interrobang : UnicodeEmoji("\u2049")

    /** Unicode emoji with a Discord shortcode of `:low_brightness:`. Represented in Unicode as 🔅. */
    object LowBrightness : UnicodeEmoji("\ud83d\udd05")

    /** Unicode emoji with a Discord shortcode of `:high_brightness:`. Represented in Unicode as 🔆. */
    object HighBrightness : UnicodeEmoji("\ud83d\udd06")

    /** Unicode emoji with a Discord shortcode of `:trident:`. Represented in Unicode as 🔱. */
    object Trident : UnicodeEmoji("\ud83d\udd31")

    /** Unicode emoji with a Discord shortcode of `:fleur_de_lis:`. Represented in Unicode as ⚜. */
    object FleurDeLis : UnicodeEmoji("\u269c")

    /** Unicode emoji with a Discord shortcode of `:part_alternation_mark:`. Represented in Unicode as 〽. */
    object PartAlternationMark : UnicodeEmoji("\u303d")

    /** Unicode emoji with a Discord shortcode of `:warning:`. Represented in Unicode as ⚠. */
    object Warning : UnicodeEmoji("\u26a0")

    /** Unicode emoji with a Discord shortcode of `:children_crossing:`. Represented in Unicode as 🚸. */
    object ChildrenCrossing : UnicodeEmoji("\ud83d\udeb8")

    /** Unicode emoji with a Discord shortcode of `:beginner:`. Represented in Unicode as 🔰. */
    object Beginner : UnicodeEmoji("\ud83d\udd30")

    /** Unicode emoji with a Discord shortcode of `:recycle:`. Represented in Unicode as ♻. */
    object Recycle : UnicodeEmoji("\u267b")

    /** Unicode emoji with a Discord shortcode of `:u6307:`. Represented in Unicode as 🈯. */
    object U6307 : UnicodeEmoji("\ud83c\ude2f")

    /** Unicode emoji with a Discord shortcode of `:chart:`. Represented in Unicode as 💹. */
    object Chart : UnicodeEmoji("\ud83d\udcb9")

    /** Unicode emoji with a Discord shortcode of `:sparkle:`. Represented in Unicode as ❇. */
    object Sparkle : UnicodeEmoji("\u2747")

    /** Unicode emoji with a Discord shortcode of `:eight_spoked_asterisk:`. Represented in Unicode as ✳. */
    object EightSpokedAsterisk : UnicodeEmoji("\u2733")

    /** Unicode emoji with a Discord shortcode of `:negative_squared_cross_mark:`. Represented in Unicode as ❎. */
    object NegativeSquaredCrossMark : UnicodeEmoji("\u274e")

    /** Unicode emoji with a Discord shortcode of `:white_check_mark:`. Represented in Unicode as ✅. */
    object WhiteCheckMark : UnicodeEmoji("\u2705")

    /** Unicode emoji with a Discord shortcode of `:diamond_shape_with_a_dot_inside:`. Represented in Unicode as 💠. */
    object DiamondShapeWithADotInside : UnicodeEmoji("\ud83d\udca0")

    /** Unicode emoji with a Discord shortcode of `:cyclone:`. Represented in Unicode as 🌀. */
    object Cyclone : UnicodeEmoji("\ud83c\udf00")

    /** Unicode emoji with a Discord shortcode of `:loop:`. Represented in Unicode as ➿. */
    object Loop : UnicodeEmoji("\u27bf")

    /** Unicode emoji with a Discord shortcode of `:globe_with_meridians:`. Represented in Unicode as 🌐. */
    object GlobeWithMeridians : UnicodeEmoji("\ud83c\udf10")

    /** Unicode emoji with a Discord shortcode of `:m:`. Represented in Unicode as Ⓜ. */
    object M : UnicodeEmoji("\u24c2")

    /** Unicode emoji with a Discord shortcode of `:atm:`. Represented in Unicode as 🏧. */
    object Atm : UnicodeEmoji("\ud83c\udfe7")

    /** Unicode emoji with a Discord shortcode of `:sa:`. Represented in Unicode as 🈂. */
    object SA : UnicodeEmoji("\ud83c\ude02")

    /** Unicode emoji with a Discord shortcode of `:passport_control:`. Represented in Unicode as 🛂. */
    object PassportControl : UnicodeEmoji("\ud83d\udec2")

    /** Unicode emoji with a Discord shortcode of `:customs:`. Represented in Unicode as 🛃. */
    object Customs : UnicodeEmoji("\ud83d\udec3")

    /** Unicode emoji with a Discord shortcode of `:baggage_claim:`. Represented in Unicode as 🛄. */
    object BaggageClaim : UnicodeEmoji("\ud83d\udec4")

    /** Unicode emoji with a Discord shortcode of `:left_luggage:`. Represented in Unicode as 🛅. */
    object LeftLuggage : UnicodeEmoji("\ud83d\udec5")

    /** Unicode emoji with a Discord shortcode of `:wheelchair:`. Represented in Unicode as ♿. */
    object Wheelchair : UnicodeEmoji("\u267f")

    /** Unicode emoji with a Discord shortcode of `:no_smoking:`. Represented in Unicode as 🚭. */
    object NoSmoking : UnicodeEmoji("\ud83d\udead")

    /** Unicode emoji with a Discord shortcode of `:wc:`. Represented in Unicode as 🚾. */
    object WC : UnicodeEmoji("\ud83d\udebe")

    /** Unicode emoji with a Discord shortcode of `:parking:`. Represented in Unicode as 🅿. */
    object Parking : UnicodeEmoji("\ud83c\udd7f")

    /** Unicode emoji with a Discord shortcode of `:potable_water:`. Represented in Unicode as 🚰. */
    object PotableWater : UnicodeEmoji("\ud83d\udeb0")

    /** Unicode emoji with a Discord shortcode of `:mens:`. Represented in Unicode as 🚹. */
    object Mens : UnicodeEmoji("\ud83d\udeb9")

    /** Unicode emoji with a Discord shortcode of `:womens:`. Represented in Unicode as 🚺. */
    object Womens : UnicodeEmoji("\ud83d\udeba")

    /** Unicode emoji with a Discord shortcode of `:baby_symbol:`. Represented in Unicode as 🚼. */
    object BabySymbol : UnicodeEmoji("\ud83d\udebc")

    /** Unicode emoji with a Discord shortcode of `:restroom:`. Represented in Unicode as 🚻. */
    object Restroom : UnicodeEmoji("\ud83d\udebb")

    /** Unicode emoji with a Discord shortcode of `:put_litter_in_its_place:`. Represented in Unicode as 🚮. */
    object PutLitterInItsPlace : UnicodeEmoji("\ud83d\udeae")

    /** Unicode emoji with a Discord shortcode of `:cinema:`. Represented in Unicode as 🎦. */
    object Cinema : UnicodeEmoji("\ud83c\udfa6")

    /** Unicode emoji with a Discord shortcode of `:signal_strength:`. Represented in Unicode as 📶. */
    object SignalStrength : UnicodeEmoji("\ud83d\udcf6")

    /** Unicode emoji with a Discord shortcode of `:koko:`. Represented in Unicode as 🈁. */
    object Koko : UnicodeEmoji("\ud83c\ude01")

    /** Unicode emoji with a Discord shortcode of `:ng:`. Represented in Unicode as 🆖. */
    object NG : UnicodeEmoji("\ud83c\udd96")

    /** Unicode emoji with a Discord shortcode of `:ok:`. Represented in Unicode as 🆗. */
    object OK : UnicodeEmoji("\ud83c\udd97")

    /** Unicode emoji with a Discord shortcode of `:up:`. Represented in Unicode as 🆙. */
    object UP : UnicodeEmoji("\ud83c\udd99")

    /** Unicode emoji with a Discord shortcode of `:cool:`. Represented in Unicode as 🆒. */
    object Cool : UnicodeEmoji("\ud83c\udd92")

    /** Unicode emoji with a Discord shortcode of `:new:`. Represented in Unicode as 🆕. */
    object New : UnicodeEmoji("\ud83c\udd95")

    /** Unicode emoji with a Discord shortcode of `:free:`. Represented in Unicode as 🆓. */
    object Free : UnicodeEmoji("\ud83c\udd93")

    /** Unicode emoji with a Discord shortcode of `:zero:`. Represented in Unicode as 0⃣. */
    object Zero : UnicodeEmoji("\u0030\u20e3")

    /** Unicode emoji with a Discord shortcode of `:one:`. Represented in Unicode as 1⃣. */
    object One : UnicodeEmoji("\u0031\u20e3")

    /** Unicode emoji with a Discord shortcode of `:two:`. Represented in Unicode as 2⃣. */
    object Two : UnicodeEmoji("\u0032\u20e3")

    /** Unicode emoji with a Discord shortcode of `:three:`. Represented in Unicode as 3⃣. */
    object Three : UnicodeEmoji("\u0033\u20e3")

    /** Unicode emoji with a Discord shortcode of `:four:`. Represented in Unicode as 4⃣. */
    object Four : UnicodeEmoji("\u0034\u20e3")

    /** Unicode emoji with a Discord shortcode of `:five:`. Represented in Unicode as 5⃣. */
    object Five : UnicodeEmoji("\u0035\u20e3")

    /** Unicode emoji with a Discord shortcode of `:six:`. Represented in Unicode as 6⃣. */
    object Six : UnicodeEmoji("\u0036\u20e3")

    /** Unicode emoji with a Discord shortcode of `:seven:`. Represented in Unicode as 7⃣. */
    object Seven : UnicodeEmoji("\u0037\u20e3")

    /** Unicode emoji with a Discord shortcode of `:eight:`. Represented in Unicode as 8⃣. */
    object Eight : UnicodeEmoji("\u0038\u20e3")

    /** Unicode emoji with a Discord shortcode of `:nine:`. Represented in Unicode as 9⃣. */
    object Nine : UnicodeEmoji("\u0039\u20e3")

    /** Unicode emoji with a Discord shortcode of `:ten:`. Represented in Unicode as 🔟. */
    object Ten : UnicodeEmoji("\ud83d\udd1f")

    /** Unicode emoji with a Discord shortcode of `:arrow_forward:`. Represented in Unicode as ▶. */
    object ArrowForward : UnicodeEmoji("\u25b6")

    /** Unicode emoji with a Discord shortcode of `:pause_button:`. Represented in Unicode as ⏸. */
    object PauseButton : UnicodeEmoji("\u23f8")

    /** Unicode emoji with a Discord shortcode of `:play_pause:`. Represented in Unicode as ⏯. */
    object PlayPause : UnicodeEmoji("\u23ef")

    /** Unicode emoji with a Discord shortcode of `:stop_button:`. Represented in Unicode as ⏹. */
    object StopButton : UnicodeEmoji("\u23f9")

    /** Unicode emoji with a Discord shortcode of `:record_button:`. Represented in Unicode as ⏺. */
    object RecordButton : UnicodeEmoji("\u23fa")

    /** Unicode emoji with a Discord shortcode of `:track_next:`. Represented in Unicode as ⏭. */
    object TrackNext : UnicodeEmoji("\u23ed")

    /** Unicode emoji with a Discord shortcode of `:track_previous:`. Represented in Unicode as ⏮. */
    object TrackPrevious : UnicodeEmoji("\u23ee")

    /** Unicode emoji with a Discord shortcode of `:fast_forward:`. Represented in Unicode as ⏩. */
    object FastForward : UnicodeEmoji("\u23e9")

    /** Unicode emoji with a Discord shortcode of `:rewind:`. Represented in Unicode as ⏪. */
    object Rewind : UnicodeEmoji("\u23ea")

    /** Unicode emoji with a Discord shortcode of `:twisted_rightwards_arrows:`. Represented in Unicode as 🔀. */
    object TwistedRightwardsArrows : UnicodeEmoji("\ud83d\udd00")

    /** Unicode emoji with a Discord shortcode of `:repeat:`. Represented in Unicode as 🔁. */
    object Repeat : UnicodeEmoji("\ud83d\udd01")

    /** Unicode emoji with a Discord shortcode of `:repeat_one:`. Represented in Unicode as 🔂. */
    object RepeatOne : UnicodeEmoji("\ud83d\udd02")

    /** Unicode emoji with a Discord shortcode of `:arrow_backward:`. Represented in Unicode as ◀. */
    object ArrowBackward : UnicodeEmoji("\u25c0")

    /** Unicode emoji with a Discord shortcode of `:arrow_up_small:`. Represented in Unicode as 🔼. */
    object ArrowUpSmall : UnicodeEmoji("\ud83d\udd3c")

    /** Unicode emoji with a Discord shortcode of `:arrow_down_small:`. Represented in Unicode as 🔽. */
    object ArrowDownSmall : UnicodeEmoji("\ud83d\udd3d")

    /** Unicode emoji with a Discord shortcode of `:arrow_double_up:`. Represented in Unicode as ⏫. */
    object ArrowDoubleUp : UnicodeEmoji("\u23eb")

    /** Unicode emoji with a Discord shortcode of `:arrow_double_down:`. Represented in Unicode as ⏬. */
    object ArrowDoubleDown : UnicodeEmoji("\u23ec")

    /** Unicode emoji with a Discord shortcode of `:arrow_right:`. Represented in Unicode as ➡. */
    object ArrowRight : UnicodeEmoji("\u27a1")

    /** Unicode emoji with a Discord shortcode of `:arrow_left:`. Represented in Unicode as ⬅. */
    object ArrowLeft : UnicodeEmoji("\u2b05")

    /** Unicode emoji with a Discord shortcode of `:arrow_up:`. Represented in Unicode as ⬆. */
    object ArrowUp : UnicodeEmoji("\u2b06")

    /** Unicode emoji with a Discord shortcode of `:arrow_down:`. Represented in Unicode as ⬇. */
    object ArrowDown : UnicodeEmoji("\u2b07")

    /** Unicode emoji with a Discord shortcode of `:arrow_upper_right:`. Represented in Unicode as ↗. */
    object ArrowUpperRight : UnicodeEmoji("\u2197")

    /** Unicode emoji with a Discord shortcode of `:arrow_lower_right:`. Represented in Unicode as ↘. */
    object ArrowLowerRight : UnicodeEmoji("\u2198")

    /** Unicode emoji with a Discord shortcode of `:arrow_lower_left:`. Represented in Unicode as ↙. */
    object ArrowLowerLeft : UnicodeEmoji("\u2199")

    /** Unicode emoji with a Discord shortcode of `:arrow_upper_left:`. Represented in Unicode as ↖. */
    object ArrowUpperLeft : UnicodeEmoji("\u2196")

    /** Unicode emoji with a Discord shortcode of `:arrow_up_down:`. Represented in Unicode as ↕. */
    object ArrowUpDown : UnicodeEmoji("\u2195")

    /** Unicode emoji with a Discord shortcode of `:left_right_arrow:`. Represented in Unicode as ↔. */
    object LeftRightArrow : UnicodeEmoji("\u2194")

    /** Unicode emoji with a Discord shortcode of `:arrows_counterclockwise:`. Represented in Unicode as 🔄. */
    object ArrowsCounterclockwise : UnicodeEmoji("\ud83d\udd04")

    /** Unicode emoji with a Discord shortcode of `:arrow_right_hook:`. Represented in Unicode as ↪. */
    object ArrowRightHook : UnicodeEmoji("\u21aa")

    /** Unicode emoji with a Discord shortcode of `:leftwards_arrow_with_hook:`. Represented in Unicode as ↩. */
    object LeftwardsArrowWithHook : UnicodeEmoji("\u21a9")

    /** Unicode emoji with a Discord shortcode of `:arrow_heading_up:`. Represented in Unicode as ⤴. */
    object ArrowHeadingUp : UnicodeEmoji("\u2934")

    /** Unicode emoji with a Discord shortcode of `:arrow_heading_down:`. Represented in Unicode as ⤵. */
    object ArrowHeadingDown : UnicodeEmoji("\u2935")

    /** Unicode emoji with a Discord shortcode of `:hash:`. Represented in Unicode as #⃣. */
    object Hash : UnicodeEmoji("\u0023\u20e3")

    /** Unicode emoji with a Discord shortcode of `:asterisk:`. Represented in Unicode as *⃣. */
    object Asterisk : UnicodeEmoji("\u002a\u20e3")

    /** Unicode emoji with a Discord shortcode of `:information_source:`. Represented in Unicode as ℹ. */
    object InformationSource : UnicodeEmoji("\u2139")

    /** Unicode emoji with a Discord shortcode of `:abc:`. Represented in Unicode as 🔤. */
    object Abc : UnicodeEmoji("\ud83d\udd24")

    /** Unicode emoji with a Discord shortcode of `:abcd:`. Represented in Unicode as 🔡. */
    object Abcd : UnicodeEmoji("\ud83d\udd21")

    /** Unicode emoji with a Discord shortcode of `:capital_abcd:`. Represented in Unicode as 🔠. */
    object CapitalAbcd : UnicodeEmoji("\ud83d\udd20")

    /** Unicode emoji with a Discord shortcode of `:symbols:`. Represented in Unicode as 🔣. */
    object Symbols : UnicodeEmoji("\ud83d\udd23")

    /** Unicode emoji with a Discord shortcode of `:musical_note:`. Represented in Unicode as 🎵. */
    object MusicalNote : UnicodeEmoji("\ud83c\udfb5")

    /** Unicode emoji with a Discord shortcode of `:notes:`. Represented in Unicode as 🎶. */
    object Notes : UnicodeEmoji("\ud83c\udfb6")

    /** Unicode emoji with a Discord shortcode of `:wavy_dash:`. Represented in Unicode as 〰. */
    object WavyDash : UnicodeEmoji("\u3030")

    /** Unicode emoji with a Discord shortcode of `:curly_loop:`. Represented in Unicode as ➰. */
    object CurlyLoop : UnicodeEmoji("\u27b0")

    /** Unicode emoji with a Discord shortcode of `:heavy_check_mark:`. Represented in Unicode as ✔. */
    object HeavyCheckMark : UnicodeEmoji("\u2714")

    /** Unicode emoji with a Discord shortcode of `:arrows_clockwise:`. Represented in Unicode as 🔃. */
    object ArrowsClockwise : UnicodeEmoji("\ud83d\udd03")

    /** Unicode emoji with a Discord shortcode of `:heavy_plus_sign:`. Represented in Unicode as ➕. */
    object HeavyPlusSign : UnicodeEmoji("\u2795")

    /** Unicode emoji with a Discord shortcode of `:heavy_minus_sign:`. Represented in Unicode as ➖. */
    object HeavyMinusSign : UnicodeEmoji("\u2796")

    /** Unicode emoji with a Discord shortcode of `:heavy_division_sign:`. Represented in Unicode as ➗. */
    object HeavyDivisionSign : UnicodeEmoji("\u2797")

    /** Unicode emoji with a Discord shortcode of `:heavy_multiplication_x:`. Represented in Unicode as ➗. */
    object HeavyMultiplicationX : UnicodeEmoji("\u2716")

    /** Unicode emoji with a Discord shortcode of `:heavy_dollar_sign:`. Represented in Unicode as 💲. */
    object HeavyDollarSign : UnicodeEmoji("\ud83d\udcb2")

    /** Unicode emoji with a Discord shortcode of `:currency_exchange:`. Represented in Unicode as 💱. */
    object CurrencyExchange : UnicodeEmoji("\ud83d\udcb1")

    /** Unicode emoji with a Discord shortcode of `:copyright:`. Represented in Unicode as ©. */
    object Copyright : UnicodeEmoji("\u00a9")

    /** Unicode emoji with a Discord shortcode of `:registered:`. Represented in Unicode as ®. */
    object Registered : UnicodeEmoji("\u00ae")

    /** Unicode emoji with a Discord shortcode of `:tm:`. Represented in Unicode as ™. */
    object TM : UnicodeEmoji("\u2122")

    /** Unicode emoji with a Discord shortcode of `:end:`. Represented in Unicode as 🔚. */
    object End : UnicodeEmoji("\ud83d\udd1a")

    /** Unicode emoji with a Discord shortcode of `:back:`. Represented in Unicode as 🔙. */
    object Back : UnicodeEmoji("\ud83d\udd19")

    /** Unicode emoji with a Discord shortcode of `:on:`. Represented in Unicode as 🔛. */
    object ON : UnicodeEmoji("\ud83d\udd1b")

    /** Unicode emoji with a Discord shortcode of `:top:`. Represented in Unicode as 🔝. */
    object Top : UnicodeEmoji("\ud83d\udd1d")

    /** Unicode emoji with a Discord shortcode of `:soon:`. Represented in Unicode as 🔜. */
    object Soon : UnicodeEmoji("\ud83d\udd1c")

    /** Unicode emoji with a Discord shortcode of `:ballot_box_with_check:`. Represented in Unicode as ☑. */
    object BallotBoxWithCheck : UnicodeEmoji("\u2611")

    /** Unicode emoji with a Discord shortcode of `:radio_button:`. Represented in Unicode as 🔘. */
    object RadioButton : UnicodeEmoji("\ud83d\udd18")

    /** Unicode emoji with a Discord shortcode of `:white_circle:`. Represented in Unicode as ⚪. */
    object WhiteCircle : UnicodeEmoji("\u26aa")

    /** Unicode emoji with a Discord shortcode of `:black_circle:`. Represented in Unicode as ⚫. */
    object BlackCircle : UnicodeEmoji("\u26ab")

    /** Unicode emoji with a Discord shortcode of `:red_circle:`. Represented in Unicode as 🔴. */
    object RedCircle : UnicodeEmoji("\ud83d\udd34")

    /** Unicode emoji with a Discord shortcode of `:large_blue_circle:`. Represented in Unicode as 🔵. */
    object LargeBlueCircle : UnicodeEmoji("\ud83d\udd35")

    /** Unicode emoji with a Discord shortcode of `:small_orange_diamond:`. Represented in Unicode as 🔸. */
    object SmallOrangeDiamond : UnicodeEmoji("\ud83d\udd38")

    /** Unicode emoji with a Discord shortcode of `:small_blue_diamond:`. Represented in Unicode as 🔹. */
    object SmallBlueDiamond : UnicodeEmoji("\ud83d\udd39")

    /** Unicode emoji with a Discord shortcode of `:large_orange_diamond:`. Represented in Unicode as 🔶. */
    object LargeOrangeDiamond : UnicodeEmoji("\ud83d\udd36")

    /** Unicode emoji with a Discord shortcode of `:large_blue_diamond:`. Represented in Unicode as 🔷. */
    object LargeBlueDiamond : UnicodeEmoji("\ud83d\udd37")

    /** Unicode emoji with a Discord shortcode of `:small_red_triangle:`. Represented in Unicode as 🔺. */
    object SmallRedTriangle : UnicodeEmoji("\ud83d\udd3a")

    /** Unicode emoji with a Discord shortcode of `:black_small_square:`. Represented in Unicode as ▪. */
    object BlackSmallSquare : UnicodeEmoji("\u25aa")

    /** Unicode emoji with a Discord shortcode of `:white_small_square:`. Represented in Unicode as ▫. */
    object WhiteSmallSquare : UnicodeEmoji("\u25ab")

    /** Unicode emoji with a Discord shortcode of `:black_large_square:`. Represented in Unicode as ⬛. */
    object BlackLargeSquare : UnicodeEmoji("\u2b1b")

    /** Unicode emoji with a Discord shortcode of `:white_large_square:`. Represented in Unicode as ⬜. */
    object WhiteLargeSquare : UnicodeEmoji("\u2b1c")

    /** Unicode emoji with a Discord shortcode of `:small_red_triangle_down:`. Represented in Unicode as 🔻. */
    object SmallRedTriangleDown : UnicodeEmoji("\ud83d\udd3b")

    /** Unicode emoji with a Discord shortcode of `:black_medium_square:`. Represented in Unicode as ◼. */
    object BlackMediumSquare : UnicodeEmoji("\u25fc")

    /** Unicode emoji with a Discord shortcode of `:white_medium_square:`. Represented in Unicode as ◻. */
    object WhiteMediumSquare : UnicodeEmoji("\u25fb")

    /** Unicode emoji with a Discord shortcode of `:black_medium_small_square:`. Represented in Unicode as ◾. */
    object BlackMediumSmallSquare : UnicodeEmoji("\u25fe")

    /** Unicode emoji with a Discord shortcode of `:white_medium_small_square:`. Represented in Unicode as ◽. */
    object WhiteMediumSmallSquare : UnicodeEmoji("\u25fd")

    /** Unicode emoji with a Discord shortcode of `:black_square_button:`. Represented in Unicode as 🔲. */
    object BlackSquareButton : UnicodeEmoji("\ud83d\udd32")

    /** Unicode emoji with a Discord shortcode of `:white_square_button:`. Represented in Unicode as 🔳. */
    object WhiteSquareButton : UnicodeEmoji("\ud83d\udd33")

    /** Unicode emoji with a Discord shortcode of `:speaker:`. Represented in Unicode as 🔈. */
    object Speaker : UnicodeEmoji("\ud83d\udd08")

    /** Unicode emoji with a Discord shortcode of `:sound:`. Represented in Unicode as 🔉. */
    object Sound : UnicodeEmoji("\ud83d\udd09")

    /** Unicode emoji with a Discord shortcode of `:loud_sound:`. Represented in Unicode as 🔊. */
    object LoudSound : UnicodeEmoji("\ud83d\udd0a")

    /** Unicode emoji with a Discord shortcode of `:mute:`. Represented in Unicode as 🔇. */
    object Mute : UnicodeEmoji("\ud83d\udd07")

    /** Unicode emoji with a Discord shortcode of `:mega:`. Represented in Unicode as 📣. */
    object Mega : UnicodeEmoji("\ud83d\udce3")

    /** Unicode emoji with a Discord shortcode of `:loudspeaker:`. Represented in Unicode as 📢. */
    object Loudspeaker : UnicodeEmoji("\ud83d\udce2")

    /** Unicode emoji with a Discord shortcode of `:bell:`. Represented in Unicode as 🔔. */
    object Bell : UnicodeEmoji("\ud83d\udd14")

    /** Unicode emoji with a Discord shortcode of `:no_bell:`. Represented in Unicode as 🔕. */
    object NoBell : UnicodeEmoji("\ud83d\udd15")

    /** Unicode emoji with a Discord shortcode of `:black_joker:`. Represented in Unicode as 🃏. */
    object BlackJoker : UnicodeEmoji("\ud83c\udccf")

    /** Unicode emoji with a Discord shortcode of `:mahjong:`. Represented in Unicode as 🀄. */
    object Mahjong : UnicodeEmoji("\ud83c\udc04")

    /** Unicode emoji with a Discord shortcode of `:spades:`. Represented in Unicode as ♠. */
    object Spades : UnicodeEmoji("\u2660")

    /** Unicode emoji with a Discord shortcode of `:clubs:`. Represented in Unicode as ♣. */
    object Clubs : UnicodeEmoji("\u2663")

    /** Unicode emoji with a Discord shortcode of `:hearts:`. Represented in Unicode as ♥. */
    object Hearts : UnicodeEmoji("\u2665")

    /** Unicode emoji with a Discord shortcode of `:diamonds:`. Represented in Unicode as ♦. */
    object Diamonds : UnicodeEmoji("\u2666")

    /** Unicode emoji with a Discord shortcode of `:flower_playing_cards:`. Represented in Unicode as 🎴. */
    object FlowerPlayingCards : UnicodeEmoji("\ud83c\udfb4")

    /** Unicode emoji with a Discord shortcode of `:thought_balloon:`. Represented in Unicode as 💭. */
    object ThoughtBalloon : UnicodeEmoji("\ud83d\udcad")

    /** Unicode emoji with a Discord shortcode of `:anger_right:`. Represented in Unicode as 🗯. */
    object AngerRight : UnicodeEmoji("\ud83d\uddef")

    /** Unicode emoji with a Discord shortcode of `:speech_balloon:`. Represented in Unicode as 💬. */
    object SpeechBalloon : UnicodeEmoji("\ud83d\udcac")

    /** Unicode emoji with a Discord shortcode of `:clock1:`. Represented in Unicode as 🕐. */
    object Clock1 : UnicodeEmoji("\ud83d\udd50")

    /** Unicode emoji with a Discord shortcode of `:clock2:`. Represented in Unicode as 🕑. */
    /** Unicode emoji with a Discord shortcode of `:clock2:`. Represented in Unicode as 🕑. */
    object Clock2 : UnicodeEmoji("\ud83d\udd51")

    /** Unicode emoji with a Discord shortcode of `:clock3:`. Represented in Unicode as 🕒. */
    object Clock3 : UnicodeEmoji("\ud83d\udd52")

    /** Unicode emoji with a Discord shortcode of `:clock4:`. Represented in Unicode as 🕓. */
    object Clock4 : UnicodeEmoji("\ud83d\udd53")

    /** Unicode emoji with a Discord shortcode of `:clock5:`. Represented in Unicode as 🕔. */
    object Clock5 : UnicodeEmoji("\ud83d\udd54")

    /** Unicode emoji with a Discord shortcode of `:clock6:`. Represented in Unicode as 🕕. */
    object Clock6 : UnicodeEmoji("\ud83d\udd55")

    /** Unicode emoji with a Discord shortcode of `:clock7:`. Represented in Unicode as 🕖. */
    object Clock7 : UnicodeEmoji("\ud83d\udd56")

    /** Unicode emoji with a Discord shortcode of `:clock8:`. Represented in Unicode as 🕗. */
    object Clock8 : UnicodeEmoji("\ud83d\udd57")

    /** Unicode emoji with a Discord shortcode of `:clock9:`. Represented in Unicode as 🕘. */
    object Clock9 : UnicodeEmoji("\ud83d\udd58")

    /** Unicode emoji with a Discord shortcode of `:clock10:`. Represented in Unicode as 🕙. */
    object Clock10 : UnicodeEmoji("\ud83d\udd59")

    /** Unicode emoji with a Discord shortcode of `:clock11:`. Represented in Unicode as 🕚. */
    object Clock11 : UnicodeEmoji("\ud83d\udd5a")

    /** Unicode emoji with a Discord shortcode of `:clock12:`. Represented in Unicode as 🕛. */
    object Clock12 : UnicodeEmoji("\ud83d\udd5b")

    /** Unicode emoji with a Discord shortcode of `:clock130:`. Represented in Unicode as 🕜. */
    object Clock130 : UnicodeEmoji("\ud83d\udd5c")

    /** Unicode emoji with a Discord shortcode of `:clock230:`. Represented in Unicode as 🕝. */
    object Clock230 : UnicodeEmoji("\ud83d\udd5d")

    /** Unicode emoji with a Discord shortcode of `:clock330:`. Represented in Unicode as 🕞. */
    object Clock330 : UnicodeEmoji("\ud83d\udd5e")

    /** Unicode emoji with a Discord shortcode of `:clock430:`. Represented in Unicode as 🕟. */
    object Clock430 : UnicodeEmoji("\ud83d\udd5f")

    /** Unicode emoji with a Discord shortcode of `:clock530:`. Represented in Unicode as 🕠. */
    object Clock530 : UnicodeEmoji("\ud83d\udd60")

    /** Unicode emoji with a Discord shortcode of `:clock630:`. Represented in Unicode as 🕡. */
    object Clock630 : UnicodeEmoji("\ud83d\udd61")

    /** Unicode emoji with a Discord shortcode of `:clock730:`. Represented in Unicode as 🕢. */
    object Clock730 : UnicodeEmoji("\ud83d\udd62")

    /** Unicode emoji with a Discord shortcode of `:clock830:`. Represented in Unicode as 🕣. */
    object Clock830 : UnicodeEmoji("\ud83d\udd63")

    /** Unicode emoji with a Discord shortcode of `:clock930:`. Represented in Unicode as 🕤. */
    object Clock930 : UnicodeEmoji("\ud83d\udd64")

    /** Unicode emoji with a Discord shortcode of `:clock1030:`. Represented in Unicode as 🕥. */
    object Clock1030 : UnicodeEmoji("\ud83d\udd65")

    /** Unicode emoji with a Discord shortcode of `:clock1130:`. Represented in Unicode as 🕦. */
    object Clock1130 : UnicodeEmoji("\ud83d\udd66")

    /** Unicode emoji with a Discord shortcode of `:clock1230:`. Represented in Unicode as 🕧. */
    object Clock1230 : UnicodeEmoji("\ud83d\udd67")

    /** Unicode emoji with a Discord shortcode of `:eye_in_speech_bubble:`. Represented in Unicode as 👁‍🗨. */
    object EyeInSpeechBubble : UnicodeEmoji("\ud83d\udc41\u200d\ud83d\udde8")

    /** Unicode emoji with a Discord shortcode of `:speech_left:`. Represented in Unicode as 🗨. */
    object SpeechLeft : UnicodeEmoji("\ud83d\udde8")

    /** Unicode emoji with a Discord shortcode of `:eject:`. Represented in Unicode as ⏏. */
    object Eject : UnicodeEmoji("\u23cf")

    /** Unicode emoji with a Discord shortcode of `:black_heart:`. Represented in Unicode as 🖤. */
    object BlackHeart : UnicodeEmoji("\ud83d\udda4")

    /** Unicode emoji with a Discord shortcode of `:stop_sign:`. Represented in Unicode as 🛑. */
    object StopSign : UnicodeEmoji("\ud83d\uded1")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_z:`. Represented in Unicode as 🇿. */
    object RegionalIndicatorZ : UnicodeEmoji("\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_y:`. Represented in Unicode as 🇾. */
    object RegionalIndicatorY : UnicodeEmoji("\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_x:`. Represented in Unicode as 🇽. */
    object RegionalIndicatorX : UnicodeEmoji("\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_w:`. Represented in Unicode as 🇼. */
    object RegionalIndicatorW : UnicodeEmoji("\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_v:`. Represented in Unicode as 🇻. */
    object RegionalIndicatorV : UnicodeEmoji("\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_u:`. Represented in Unicode as 🇺. */
    object RegionalIndicatorU : UnicodeEmoji("\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_t:`. Represented in Unicode as 🇹. */
    object RegionalIndicatorT : UnicodeEmoji("\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_s:`. Represented in Unicode as 🇸. */
    object RegionalIndicatorS : UnicodeEmoji("\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_r:`. Represented in Unicode as 🇷. */
    object RegionalIndicatorR : UnicodeEmoji("\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_q:`. Represented in Unicode as 🇶. */
    object RegionalIndicatorQ : UnicodeEmoji("\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_p:`. Represented in Unicode as 🇵. */
    object RegionalIndicatorP : UnicodeEmoji("\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_o:`. Represented in Unicode as 🇴. */
    object RegionalIndicatorO : UnicodeEmoji("\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_n:`. Represented in Unicode as 🇳. */
    object RegionalIndicatorN : UnicodeEmoji("\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_m:`. Represented in Unicode as 🇲. */
    object RegionalIndicatorM : UnicodeEmoji("\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_l:`. Represented in Unicode as 🇱. */
    object RegionalIndicatorL : UnicodeEmoji("\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_k:`. Represented in Unicode as 🇰. */
    object RegionalIndicatorK : UnicodeEmoji("\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_j:`. Represented in Unicode as 🇯. */
    object RegionalIndicatorJ : UnicodeEmoji("\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_i:`. Represented in Unicode as 🇮. */
    object RegionalIndicatorI : UnicodeEmoji("\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_h:`. Represented in Unicode as 🇭. */
    object RegionalIndicatorH : UnicodeEmoji("\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_g:`. Represented in Unicode as 🇬. */
    object RegionalIndicatorG : UnicodeEmoji("\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_f:`. Represented in Unicode as 🇫. */
    object RegionalIndicatorF : UnicodeEmoji("\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_e:`. Represented in Unicode as 🇪. */
    object RegionalIndicatorE : UnicodeEmoji("\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_d:`. Represented in Unicode as 🇩. */
    object RegionalIndicatorD : UnicodeEmoji("\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_c:`. Represented in Unicode as 🇨. */
    object RegionalIndicatorC : UnicodeEmoji("\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_b:`. Represented in Unicode as 🇧. */
    object RegionalIndicatorB : UnicodeEmoji("\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:regional_indicator_a:`. Represented in Unicode as 🇦. */
    object RegionalIndicatorA : UnicodeEmoji("\ud83c\udde6")

    // Flags

    /** Unicode emoji with a Discord shortcode of `:flag_ac:`. Represented in Unicode as 🇦🇨. */
    object FlagAC : UnicodeEmoji("\ud83c\udde6\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_ad:`. Represented in Unicode as 🇦🇩. */
    object FlagAD : UnicodeEmoji("\ud83c\udde6\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ae:`. Represented in Unicode as 🇦🇪. */
    object FlagAE : UnicodeEmoji("\ud83c\udde6\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_af:`. Represented in Unicode as 🇦🇫. */
    object FlagAF : UnicodeEmoji("\ud83c\udde6\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_ag:`. Represented in Unicode as 🇦🇬. */
    object FlagAG : UnicodeEmoji("\ud83c\udde6\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ai:`. Represented in Unicode as 🇦🇮. */
    object FlagAI : UnicodeEmoji("\ud83c\udde6\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_al:`. Represented in Unicode as 🇦🇱. */
    object FlagAL : UnicodeEmoji("\ud83c\udde6\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_am:`. Represented in Unicode as 🇦🇲. */
    object FlagAM : UnicodeEmoji("\ud83c\udde6\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_ao:`. Represented in Unicode as 🇦🇴. */
    object FlagAO : UnicodeEmoji("\ud83c\udde6\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_aq:`. Represented in Unicode as 🇦🇶. */
    object FlagAQ : UnicodeEmoji("\ud83c\udde6\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_ar:`. Represented in Unicode as 🇦🇷. */
    object FlagAR : UnicodeEmoji("\ud83c\udde6\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_as:`. Represented in Unicode as 🇦🇸. */
    object FlagAS : UnicodeEmoji("\ud83c\udde6\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_at:`. Represented in Unicode as 🇦🇹. */
    object FlagAT : UnicodeEmoji("\ud83c\udde6\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_au:`. Represented in Unicode as 🇦🇺. */
    object FlagAU : UnicodeEmoji("\ud83c\udde6\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_aw:`. Represented in Unicode as 🇦🇼. */
    object FlagAW : UnicodeEmoji("\ud83c\udde6\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_ax:`. Represented in Unicode as 🇦🇽. */
    object FlagAX : UnicodeEmoji("\ud83c\udde6\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_az:`. Represented in Unicode as 🇦🇿. */
    object FlagAZ : UnicodeEmoji("\ud83c\udde6\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ba:`. Represented in Unicode as 🇧🇦. */
    object FlagBA : UnicodeEmoji("\ud83c\udde7\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_bb:`. Represented in Unicode as 🇧🇧. */
    object FlagBB : UnicodeEmoji("\ud83c\udde7\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_bd:`. Represented in Unicode as 🇧🇩. */
    object FlagBD : UnicodeEmoji("\ud83c\udde7\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_be:`. Represented in Unicode as 🇧🇪. */
    object FlagBE : UnicodeEmoji("\ud83c\udde7\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_bf:`. Represented in Unicode as 🇧🇫. */
    object FlagBF : UnicodeEmoji("\ud83c\udde7\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_bg:`. Represented in Unicode as 🇧🇬. */
    object FlagBG : UnicodeEmoji("\ud83c\udde7\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_bh:`. Represented in Unicode as 🇧🇭. */
    object FlagBH : UnicodeEmoji("\ud83c\udde7\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_bi:`. Represented in Unicode as 🇧🇮. */
    object FlagBI : UnicodeEmoji("\ud83c\udde7\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_bj:`. Represented in Unicode as 🇧🇯. */
    object FlagBJ : UnicodeEmoji("\ud83c\udde7\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_bl:`. Represented in Unicode as 🇧🇱. */
    object FlagBL : UnicodeEmoji("\ud83c\udde7\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_bm:`. Represented in Unicode as 🇧🇲. */
    object FlagBM : UnicodeEmoji("\ud83c\udde7\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_bn:`. Represented in Unicode as 🇧🇳. */
    object FlagBN : UnicodeEmoji("\ud83c\udde7\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_bo:`. Represented in Unicode as 🇧🇴. */
    object FlagBO : UnicodeEmoji("\ud83c\udde7\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_bq:`. Represented in Unicode as 🇧🇶. */
    object FlagBQ : UnicodeEmoji("\ud83c\udde7\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_br:`. Represented in Unicode as 🇧🇷. */
    object FlagBR : UnicodeEmoji("\ud83c\udde7\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_bs:`. Represented in Unicode as 🇧🇸. */
    object FlagBS : UnicodeEmoji("\ud83c\udde7\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_bt:`. Represented in Unicode as 🇧🇹. */
    object FlagBT : UnicodeEmoji("\ud83c\udde7\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_bv:`. Represented in Unicode as 🇧🇻. */
    object FlagBV : UnicodeEmoji("\ud83c\udde7\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_bw:`. Represented in Unicode as 🇧🇼. */
    object FlagBW : UnicodeEmoji("\ud83c\udde7\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_by:`. Represented in Unicode as 🇧🇾. */
    object FlagBY : UnicodeEmoji("\ud83c\udde7\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_bz:`. Represented in Unicode as 🇧🇿. */
    object FlagBZ : UnicodeEmoji("\ud83c\udde7\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ca:`. Represented in Unicode as 🇨🇦. */
    object FlagCA : UnicodeEmoji("\ud83c\udde8\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_cc:`. Represented in Unicode as 🇨🇨. */
    object FlagCC : UnicodeEmoji("\ud83c\udde8\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_cd:`. Represented in Unicode as 🇨🇩. */
    object FlagCD : UnicodeEmoji("\ud83c\udde8\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_cf:`. Represented in Unicode as 🇨🇫. */
    object FlagCF : UnicodeEmoji("\ud83c\udde8\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_cg:`. Represented in Unicode as 🇨🇬. */
    object FlagCG : UnicodeEmoji("\ud83c\udde8\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ch:`. Represented in Unicode as 🇨🇭. */
    object FlagCH : UnicodeEmoji("\ud83c\udde8\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_ci:`. Represented in Unicode as 🇨🇮. */
    object FlagCI : UnicodeEmoji("\ud83c\udde8\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_ck:`. Represented in Unicode as 🇨🇰. */
    object FlagCK : UnicodeEmoji("\ud83c\udde8\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_cl:`. Represented in Unicode as 🇨🇱. */
    object FlagCL : UnicodeEmoji("\ud83c\udde8\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_cm:`. Represented in Unicode as 🇨🇲. */
    object FlagCM : UnicodeEmoji("\ud83c\udde8\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_cn:`. Represented in Unicode as 🇨🇳. */
    object FlagCN : UnicodeEmoji("\ud83c\udde8\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_co:`. Represented in Unicode as 🇨🇴. */
    object FlagCO : UnicodeEmoji("\ud83c\udde8\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_cp:`. Represented in Unicode as 🇨🇵. */
    object FlagCP : UnicodeEmoji("\ud83c\udde8\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_cr:`. Represented in Unicode as 🇨🇷. */
    object FlagCR : UnicodeEmoji("\ud83c\udde8\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_cu:`. Represented in Unicode as 🇨🇺. */
    object FlagCU : UnicodeEmoji("\ud83c\udde8\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_cv:`. Represented in Unicode as 🇨🇻. */
    object FlagCV : UnicodeEmoji("\ud83c\udde8\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_cw:`. Represented in Unicode as 🇨🇼. */
    object FlagCW : UnicodeEmoji("\ud83c\udde8\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_cx:`. Represented in Unicode as 🇨🇽. */
    object FlagCX : UnicodeEmoji("\ud83c\udde8\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_cy:`. Represented in Unicode as 🇨🇾. */
    object FlagCY : UnicodeEmoji("\ud83c\udde8\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_cz:`. Represented in Unicode as 🇨🇿. */
    object FlagCZ : UnicodeEmoji("\ud83c\udde8\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_de:`. Represented in Unicode as 🇩🇪. */
    object FlagDE : UnicodeEmoji("\ud83c\udde9\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_dg:`. Represented in Unicode as 🇩🇬. */
    object FlagDG : UnicodeEmoji("\ud83c\udde9\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_dj:`. Represented in Unicode as 🇩🇯. */
    object FlagDJ : UnicodeEmoji("\ud83c\udde9\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_dk:`. Represented in Unicode as 🇩🇰. */
    object FlagDK : UnicodeEmoji("\ud83c\udde9\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_dm:`. Represented in Unicode as 🇩🇲. */
    object FlagDM : UnicodeEmoji("\ud83c\udde9\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_do:`. Represented in Unicode as 🇩🇴. */
    object FlagDO : UnicodeEmoji("\ud83c\udde9\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_dz:`. Represented in Unicode as 🇩🇿. */
    object FlagDZ : UnicodeEmoji("\ud83c\udde9\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ea:`. Represented in Unicode as 🇪🇦. */
    object FlagEA : UnicodeEmoji("\ud83c\uddea\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_ec:`. Represented in Unicode as 🇪🇨. */
    object FlagEC : UnicodeEmoji("\ud83c\uddea\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_ee:`. Represented in Unicode as 🇪🇪. */
    object FlagEE : UnicodeEmoji("\ud83c\uddea\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_eg:`. Represented in Unicode as 🇪🇬. */
    object FlagEG : UnicodeEmoji("\ud83c\uddea\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_eh:`. Represented in Unicode as 🇪🇭. */
    object FlagEH : UnicodeEmoji("\ud83c\uddea\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_er:`. Represented in Unicode as 🇪🇷. */
    object FlagER : UnicodeEmoji("\ud83c\uddea\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_es:`. Represented in Unicode as 🇪🇸. */
    object FlagES : UnicodeEmoji("\ud83c\uddea\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_et:`. Represented in Unicode as 🇪🇹. */
    object FlagET : UnicodeEmoji("\ud83c\uddea\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_eu:`. Represented in Unicode as 🇪🇺. */
    object FlagEU : UnicodeEmoji("\ud83c\uddea\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_fi:`. Represented in Unicode as 🇫🇮. */
    object FlagFI : UnicodeEmoji("\ud83c\uddeb\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_fj:`. Represented in Unicode as 🇫🇯. */
    object FlagFJ : UnicodeEmoji("\ud83c\uddeb\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_fk:`. Represented in Unicode as 🇫🇰. */
    object FlagFK : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_fm:`. Represented in Unicode as 🇫🇲. */
    object FlagFM : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_fo:`. Represented in Unicode as 🇫🇴. */
    object FlagFO : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_fr:`. Represented in Unicode as 🇫🇷. */
    object FlagFR : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ga:`. Represented in Unicode as 🇬🇦. */
    object FlagGA : UnicodeEmoji("\ud83c\uddec\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_gb:`. Represented in Unicode as 🇬🇧. */
    object FlagGB : UnicodeEmoji("\ud83c\uddec\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_gd:`. Represented in Unicode as 🇬🇩. */
    object FlagGD : UnicodeEmoji("\ud83c\uddec\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ge:`. Represented in Unicode as 🇬🇪. */
    object FlagGE : UnicodeEmoji("\ud83c\uddec\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_gf:`. Represented in Unicode as 🇬🇫. */
    object FlagGF : UnicodeEmoji("\ud83c\uddec\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_gg:`. Represented in Unicode as 🇬🇬. */
    object FlagGG : UnicodeEmoji("\ud83c\uddec\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_gh:`. Represented in Unicode as 🇬🇭. */
    object FlagGH : UnicodeEmoji("\ud83c\uddec\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_gi:`. Represented in Unicode as 🇬🇮. */
    object FlagGI : UnicodeEmoji("\ud83c\uddec\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_gl:`. Represented in Unicode as 🇬🇱. */
    object FlagGL : UnicodeEmoji("\ud83c\uddec\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_gm:`. Represented in Unicode as 🇬🇲. */
    object FlagGM : UnicodeEmoji("\ud83c\uddec\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_gn:`. Represented in Unicode as 🇬🇳. */
    object FlagGN : UnicodeEmoji("\ud83c\uddec\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_gp:`. Represented in Unicode as 🇬🇵. */
    object FlagGP : UnicodeEmoji("\ud83c\uddec\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_gq:`. Represented in Unicode as 🇬🇶. */
    object FlagGQ : UnicodeEmoji("\ud83c\uddec\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_gr:`. Represented in Unicode as 🇬🇷. */
    object FlagGR : UnicodeEmoji("\ud83c\uddec\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_gs:`. Represented in Unicode as 🇬🇸. */
    object FlagGS : UnicodeEmoji("\ud83c\uddec\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_gt:`. Represented in Unicode as 🇬🇹. */
    object FlagGT : UnicodeEmoji("\ud83c\uddec\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_gu:`. Represented in Unicode as 🇬🇺. */
    object FlagGU : UnicodeEmoji("\ud83c\uddec\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_gw:`. Represented in Unicode as 🇬🇼. */
    object FlagGW : UnicodeEmoji("\ud83c\uddec\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_gy:`. Represented in Unicode as 🇬🇾. */
    object FlagGY : UnicodeEmoji("\ud83c\uddec\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_hk:`. Represented in Unicode as 🇭🇰. */
    object FlagHK : UnicodeEmoji("\ud83c\udded\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_hm:`. Represented in Unicode as 🇭🇲. */
    object FlagHM : UnicodeEmoji("\ud83c\udded\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_hn:`. Represented in Unicode as 🇭🇳. */
    object FlagHN : UnicodeEmoji("\ud83c\udded\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_hr:`. Represented in Unicode as 🇭🇷. */
    object FlagHR : UnicodeEmoji("\ud83c\udded\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ht:`. Represented in Unicode as 🇭🇹. */
    object FlagHT : UnicodeEmoji("\ud83c\udded\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_hu:`. Represented in Unicode as 🇭🇺. */
    object FlagHU : UnicodeEmoji("\ud83c\udded\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_ic:`. Represented in Unicode as 🇮🇨. */
    object FlagIC : UnicodeEmoji("\ud83c\uddee\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_id:`. Represented in Unicode as 🇮🇩. */
    object FlagID : UnicodeEmoji("\ud83c\uddee\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ie:`. Represented in Unicode as 🇮🇪. */
    object FlagIE : UnicodeEmoji("\ud83c\uddee\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_il:`. Represented in Unicode as 🇮🇱. */
    object FlagIL : UnicodeEmoji("\ud83c\uddee\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_im:`. Represented in Unicode as 🇮🇲. */
    object FlagIM : UnicodeEmoji("\ud83c\uddee\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_in:`. Represented in Unicode as 🇮🇳. */
    object FlagIN : UnicodeEmoji("\ud83c\uddee\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_io:`. Represented in Unicode as 🇮🇴. */
    object FlagIO : UnicodeEmoji("\ud83c\uddee\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_iq:`. Represented in Unicode as 🇮🇶. */
    object FlagIQ : UnicodeEmoji("\ud83c\uddee\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_ir:`. Represented in Unicode as 🇮🇷. */
    object FlagIR : UnicodeEmoji("\ud83c\uddee\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_is:`. Represented in Unicode as 🇮🇸. */
    object FlagIS : UnicodeEmoji("\ud83c\uddee\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_it:`. Represented in Unicode as 🇮🇹. */
    object FlagIT : UnicodeEmoji("\ud83c\uddee\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_je:`. Represented in Unicode as 🇯🇪. */
    object FlagJE : UnicodeEmoji("\ud83c\uddef\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_jm:`. Represented in Unicode as 🇯🇲. */
    object FlagJM : UnicodeEmoji("\ud83c\uddef\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_jo:`. Represented in Unicode as 🇯🇴. */
    object FlagJO : UnicodeEmoji("\ud83c\uddef\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_jp:`. Represented in Unicode as 🇯🇵. */
    object FlagJP : UnicodeEmoji("\ud83c\uddef\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_ke:`. Represented in Unicode as 🇰🇪. */
    object FlagKE : UnicodeEmoji("\ud83c\uddf0\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_kg:`. Represented in Unicode as 🇰🇬. */
    object FlagKG : UnicodeEmoji("\ud83c\uddf0\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_kh:`. Represented in Unicode as 🇰🇭. */
    object FlagKH : UnicodeEmoji("\ud83c\uddf0\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_ki:`. Represented in Unicode as 🇰🇮. */
    object FlagKI : UnicodeEmoji("\ud83c\uddf0\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_km:`. Represented in Unicode as 🇰🇲. */
    object FlagKM : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_kn:`. Represented in Unicode as 🇰🇳. */
    object FlagKN : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_kp:`. Represented in Unicode as 🇰🇵. */
    object FlagKP : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_kr:`. Represented in Unicode as 🇰🇷. */
    object FlagKR : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_kw:`. Represented in Unicode as 🇰🇼. */
    object FlagKW : UnicodeEmoji("\ud83c\uddf0\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_ky:`. Represented in Unicode as 🇰🇾. */
    object FlagKY : UnicodeEmoji("\ud83c\uddf0\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_kz:`. Represented in Unicode as 🇰🇿. */
    object FlagKZ : UnicodeEmoji("\ud83c\uddf0\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_la:`. Represented in Unicode as 🇱🇦. */
    object FlagLA : UnicodeEmoji("\ud83c\uddf1\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_lb:`. Represented in Unicode as 🇱🇧. */
    object FlagLB : UnicodeEmoji("\ud83c\uddf1\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_lc:`. Represented in Unicode as 🇱🇨. */
    object FlagLC : UnicodeEmoji("\ud83c\uddf1\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_li:`. Represented in Unicode as 🇱🇮. */
    object FlagLI : UnicodeEmoji("\ud83c\uddf1\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_lk:`. Represented in Unicode as 🇱🇰. */
    object FlagLK : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_lr:`. Represented in Unicode as 🇱🇷. */
    object FlagLR : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ls:`. Represented in Unicode as 🇱🇸. */
    object FlagLS : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_lt:`. Represented in Unicode as 🇱🇹. */
    object FlagLT : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_lu:`. Represented in Unicode as 🇱🇺. */
    object FlagLU : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_lv:`. Represented in Unicode as 🇱🇻. */
    object FlagLV : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_ly:`. Represented in Unicode as 🇱🇾. */
    object FlagLY : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_ma:`. Represented in Unicode as 🇲🇦. */
    object FlagMA : UnicodeEmoji("\ud83c\uddf2\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_mc:`. Represented in Unicode as 🇲🇨. */
    object FlagMC : UnicodeEmoji("\ud83c\uddf2\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_md:`. Represented in Unicode as 🇲🇩. */
    object FlagMD : UnicodeEmoji("\ud83c\uddf2\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_me:`. Represented in Unicode as 🇲🇪. */
    object FlagME : UnicodeEmoji("\ud83c\uddf2\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_mf:`. Represented in Unicode as 🇲🇫. */
    object FlagMF : UnicodeEmoji("\ud83c\uddf2\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_mg:`. Represented in Unicode as 🇲🇬. */
    object FlagMG : UnicodeEmoji("\ud83c\uddf2\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_mh:`. Represented in Unicode as 🇲🇭. */
    object FlagMH : UnicodeEmoji("\ud83c\uddf2\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_mk:`. Represented in Unicode as 🇲🇰. */
    object FlagMK : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_ml:`. Represented in Unicode as 🇲🇱. */
    object FlagML : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_mm:`. Represented in Unicode as 🇲🇲. */
    object FlagMM : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_mn:`. Represented in Unicode as 🇲🇳. */
    object FlagMN : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_mo:`. Represented in Unicode as 🇲🇴. */
    object FlagMO : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_mp:`. Represented in Unicode as 🇲🇵. */
    object FlagMP : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_mq:`. Represented in Unicode as 🇲🇶. */
    object FlagMQ : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_mr:`. Represented in Unicode as 🇲🇷. */
    object FlagMR : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ms:`. Represented in Unicode as 🇲🇸. */
    object FlagMS : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_mt:`. Represented in Unicode as 🇲🇹. */
    object FlagMT : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_mu:`. Represented in Unicode as 🇲🇺. */
    object FlagMU : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_mv:`. Represented in Unicode as 🇲🇻. */
    object FlagMV : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_mw:`. Represented in Unicode as 🇲🇼. */
    object FlagMW : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_mx:`. Represented in Unicode as 🇲🇽. */
    object FlagMX : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_my:`. Represented in Unicode as 🇲🇾. */
    object FlagMY : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_mz:`. Represented in Unicode as 🇲🇿. */
    object FlagMZ : UnicodeEmoji("\ud83c\uddf2\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_na:`. Represented in Unicode as 🇳🇦. */
    object FlagNA : UnicodeEmoji("\ud83c\uddf3\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_nc:`. Represented in Unicode as 🇳🇨. */
    object FlagNC : UnicodeEmoji("\ud83c\uddf3\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_ne:`. Represented in Unicode as 🇳🇪. */
    object FlagNE : UnicodeEmoji("\ud83c\uddf3\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_nf:`. Represented in Unicode as 🇳🇫. */
    object FlagNF : UnicodeEmoji("\ud83c\uddf3\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_ng:`. Represented in Unicode as 🇳🇬. */
    object FlagNG : UnicodeEmoji("\ud83c\uddf3\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ni:`. Represented in Unicode as 🇳🇮. */
    object FlagNI : UnicodeEmoji("\ud83c\uddf3\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_nl:`. Represented in Unicode as 🇳🇱. */
    object FlagNL : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_no:`. Represented in Unicode as 🇳🇴. */
    object FlagNO : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_np:`. Represented in Unicode as 🇳🇵. */
    object FlagNP : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_nr:`. Represented in Unicode as 🇳🇷. */
    object FlagNR : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_nu:`. Represented in Unicode as 🇳🇺. */
    object FlagNU : UnicodeEmoji("\ud83c\uddf3\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_nz:`. Represented in Unicode as 🇳🇿. */
    object FlagNZ : UnicodeEmoji("\ud83c\uddf3\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_om:`. Represented in Unicode as 🇴🇲. */
    object FlagOM : UnicodeEmoji("\ud83c\uddf4\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_pa:`. Represented in Unicode as 🇵🇦. */
    object FlagPA : UnicodeEmoji("\ud83c\uddf5\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_pe:`. Represented in Unicode as 🇵🇪. */
    object FlagPE : UnicodeEmoji("\ud83c\uddf5\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_pf:`. Represented in Unicode as 🇵🇫. */
    object FlagPF : UnicodeEmoji("\ud83c\uddf5\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_pg:`. Represented in Unicode as 🇵🇬. */
    object FlagPG : UnicodeEmoji("\ud83c\uddf5\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ph:`. Represented in Unicode as 🇵🇭. */
    object FlagPH : UnicodeEmoji("\ud83c\uddf5\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_pk:`. Represented in Unicode as 🇵🇰. */
    object FlagPK : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_pl:`. Represented in Unicode as 🇵🇱. */
    object FlagPL : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_pm:`. Represented in Unicode as 🇵🇲. */
    object FlagPM : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_pn:`. Represented in Unicode as 🇵🇳. */
    object FlagPN : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_pr:`. Represented in Unicode as 🇵🇷. */
    object FlagPR : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ps:`. Represented in Unicode as 🇵🇸. */
    object FlagPS : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_pt:`. Represented in Unicode as 🇵🇹. */
    object FlagPT : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_pw:`. Represented in Unicode as 🇵🇼. */
    object FlagPW : UnicodeEmoji("\ud83c\uddf5\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_py:`. Represented in Unicode as 🇵🇾. */
    object FlagPY : UnicodeEmoji("\ud83c\uddf5\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_qa:`. Represented in Unicode as 🇶🇦. */
    object FlagQA : UnicodeEmoji("\ud83c\uddf6\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_re:`. Represented in Unicode as 🇷🇪. */
    object FlagRE : UnicodeEmoji("\ud83c\uddf7\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ro:`. Represented in Unicode as 🇷🇴. */
    object FlagRO : UnicodeEmoji("\ud83c\uddf7\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_rs:`. Represented in Unicode as 🇷🇸. */
    object FlagRS : UnicodeEmoji("\ud83c\uddf7\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_ru:`. Represented in Unicode as 🇷🇺. */
    object FlagRU : UnicodeEmoji("\ud83c\uddf7\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_rw:`. Represented in Unicode as 🇷🇼. */
    object FlagRW : UnicodeEmoji("\ud83c\uddf7\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_sa:`. Represented in Unicode as 🇸🇦. */
    object FlagSA : UnicodeEmoji("\ud83c\uddf8\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_sb:`. Represented in Unicode as 🇸🇧. */
    object FlagSB : UnicodeEmoji("\ud83c\uddf8\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_sc:`. Represented in Unicode as 🇸🇨. */
    object FlagSC : UnicodeEmoji("\ud83c\uddf8\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_sd:`. Represented in Unicode as 🇸🇩. */
    object FlagSD : UnicodeEmoji("\ud83c\uddf8\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_se:`. Represented in Unicode as 🇸🇪. */
    object FlagSE : UnicodeEmoji("\ud83c\uddf8\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_sg:`. Represented in Unicode as 🇸🇬. */
    object FlagSG : UnicodeEmoji("\ud83c\uddf8\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_sh:`. Represented in Unicode as 🇸🇭. */
    object FlagSH : UnicodeEmoji("\ud83c\uddf8\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_si:`. Represented in Unicode as 🇸🇮. */
    object FlagSI : UnicodeEmoji("\ud83c\uddf8\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_sj:`. Represented in Unicode as 🇸🇯. */
    object FlagSJ : UnicodeEmoji("\ud83c\uddf8\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_sk:`. Represented in Unicode as 🇸🇰. */
    object FlagSK : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_sl:`. Represented in Unicode as 🇸🇱. */
    object FlagSL : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_sm:`. Represented in Unicode as 🇸🇲. */
    object FlagSM : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_sn:`. Represented in Unicode as 🇸🇳. */
    object FlagSN : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_so:`. Represented in Unicode as 🇸🇴. */
    object FlagSO : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_sr:`. Represented in Unicode as 🇸🇷. */
    object FlagSR : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ss:`. Represented in Unicode as 🇸🇸. */
    object FlagSS : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_st:`. Represented in Unicode as 🇸🇹. */
    object FlagST : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_sv:`. Represented in Unicode as 🇸🇻. */
    object FlagSV : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_sx:`. Represented in Unicode as 🇸🇽. */
    object FlagSX : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_sy:`. Represented in Unicode as 🇸🇾. */
    object FlagSY : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_sz:`. Represented in Unicode as 🇸🇿. */
    object FlagSZ : UnicodeEmoji("\ud83c\uddf8\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ta:`. Represented in Unicode as 🇹🇦. */
    object FlagTA : UnicodeEmoji("\ud83c\uddf9\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_tc:`. Represented in Unicode as 🇹🇨. */
    object FlagTC : UnicodeEmoji("\ud83c\uddf9\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_td:`. Represented in Unicode as 🇹🇩. */
    object FlagTD : UnicodeEmoji("\ud83c\uddf9\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_tf:`. Represented in Unicode as 🇹🇫. */
    object FlagTF : UnicodeEmoji("\ud83c\uddf9\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_tg:`. Represented in Unicode as 🇹🇬. */
    object FlagTG : UnicodeEmoji("\ud83c\uddf9\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_th:`. Represented in Unicode as 🇹🇭. */
    object FlagTH : UnicodeEmoji("\ud83c\uddf9\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_tj:`. Represented in Unicode as 🇹🇯. */
    object FlagTJ : UnicodeEmoji("\ud83c\uddf9\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_tk:`. Represented in Unicode as 🇹🇰. */
    object FlagTK : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_tl:`. Represented in Unicode as 🇹🇱. */
    object FlagTL : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_tm:`. Represented in Unicode as 🇹🇲. */
    object FlagTM : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_tn:`. Represented in Unicode as 🇹🇳. */
    object FlagTN : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_to:`. Represented in Unicode as 🇹🇴. */
    object FlagTO : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_tr:`. Represented in Unicode as 🇹🇷. */
    object FlagTR : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_tt:`. Represented in Unicode as 🇹🇹. */
    object FlagTT : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_tv:`. Represented in Unicode as 🇹🇻. */
    object FlagTV : UnicodeEmoji("\ud83c\uddf9\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_tw:`. Represented in Unicode as 🇹🇼. */
    object FlagTW : UnicodeEmoji("\ud83c\uddf9\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_tz:`. Represented in Unicode as 🇹🇿. */
    object FlagTZ : UnicodeEmoji("\ud83c\uddf9\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ua:`. Represented in Unicode as 🇺🇦. */
    object FlagUA : UnicodeEmoji("\ud83c\uddfa\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_ug:`. Represented in Unicode as 🇺🇬. */
    object FlagUG : UnicodeEmoji("\ud83c\uddfa\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_um:`. Represented in Unicode as 🇺🇲. */
    object FlagUM : UnicodeEmoji("\ud83c\uddfa\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_us:`. Represented in Unicode as 🇺🇸. */
    object FlagUS : UnicodeEmoji("\ud83c\uddfa\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_uy:`. Represented in Unicode as 🇺🇾. */
    object FlagUY : UnicodeEmoji("\ud83c\uddfa\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_uz:`. Represented in Unicode as 🇺🇿. */
    object FlagUZ : UnicodeEmoji("\ud83c\uddfa\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_va:`. Represented in Unicode as 🇻🇦. */
    object FlagVA : UnicodeEmoji("\ud83c\uddfb\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_vc:`. Represented in Unicode as 🇻🇨. */
    object FlagVC : UnicodeEmoji("\ud83c\uddfb\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_ve:`. Represented in Unicode as 🇻🇪. */
    object FlagVE : UnicodeEmoji("\ud83c\uddfb\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_vg:`. Represented in Unicode as 🇻🇬. */
    object FlagVG : UnicodeEmoji("\ud83c\uddfb\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_vi:`. Represented in Unicode as 🇻🇮. */
    object FlagVI : UnicodeEmoji("\ud83c\uddfb\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_vn:`. Represented in Unicode as 🇻🇳. */
    object FlagVN : UnicodeEmoji("\ud83c\uddfb\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_vu:`. Represented in Unicode as 🇻🇺. */
    object FlagVU : UnicodeEmoji("\ud83c\uddfb\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_wf:`. Represented in Unicode as 🇼🇫. */
    object FlagWF : UnicodeEmoji("\ud83c\uddfc\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_ws:`. Represented in Unicode as 🇼🇸. */
    object FlagWS : UnicodeEmoji("\ud83c\uddfc\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_xk:`. Represented in Unicode as 🇽🇰. */
    object FlagXK : UnicodeEmoji("\ud83c\uddfd\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_ye:`. Represented in Unicode as 🇾🇪. */
    object FlagYE : UnicodeEmoji("\ud83c\uddfe\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_yt:`. Represented in Unicode as 🇾🇹. */
    object FlagYT : UnicodeEmoji("\ud83c\uddfe\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_za:`. Represented in Unicode as 🇿🇦. */
    object FlagZA : UnicodeEmoji("\ud83c\uddff\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_zm:`. Represented in Unicode as 🇿🇲. */
    object FlagZM : UnicodeEmoji("\ud83c\uddff\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_zw:`. Represented in Unicode as 🇿🇼. */
    object FlagZW : UnicodeEmoji("\ud83c\uddff\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:gay_pride_flag:`. Represented in Unicode as 🏳️‍🌈. */
    object GayPrideFlag : UnicodeEmoji("\ud83c\udff3\ufe0f\u200d\ud83c\udf08")

    /** Unknown unicode emoji provided by the user or discord. */
    class Unknown internal constructor(unicode: String) : UnicodeEmoji(unicode)

    /** Get this [UnicodeEmoji] as [String] (aka [combinedUnicode]). */
    override fun toString(): String = combinedUnicode

    /** Check if [other] is the same as this [UnicodeEmoji]. */
    override fun equals(other: Any?): Boolean = other.toString() == toString()

    companion object {
        /** A list of 0-10 emojis. */
        val numbers: List<UnicodeEmoji> = listOf(Zero, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten)

        /** Creates a unicode emoji from the given [unicode] string. */
        operator fun invoke(unicode: String): UnicodeEmoji = Unknown(unicode)
    }
}

/**
 * An enum containing a list of supported skin tones. Can be used in a [UnicodeEmoji]'s constructor if it supports
 * skin tones.
 *
 * @property unicode The unicode representation of this skin tone.
 */
enum class SkinTone(val unicode: String) {
    /** The lightest skin tone, represented in Discord shortcode as `:skin-tone-1:`. */
    LIGHT("\ud83c\udffb"),
    /** A medium-light skin tone, represented in Discord shortcode as `:skin-tone-2:`. */
    MEDIUM_LIGHT("\ud83c\udffc"),
    /** A medium skin tone, represented in Discord shortcode as `:skin-tone-3:`. */
    MEDIUM("\ud83c\udffd"),
    /** A medium-dark skin tone, represented in Discord shortcode as `:skin-tone-4:`. */
    MEDIUM_DARK("\ud83c\udffe"),
    /** The darkest skin tone, represented in Discord shortcode as `:skin-tone-5:`. */
    DARK("\ud83c\udfff")
}

/** Converts a [PartialEmojiPacket] to an [Emoji]. */
internal fun PartialEmojiPacket.toEmoji(context: BotClient): Emoji =
    id?.let { ForeignGuildEmoji(context, id, name) } ?: UnicodeEmoji(name)
