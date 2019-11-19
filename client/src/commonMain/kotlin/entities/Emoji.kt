@file:Suppress("unused")

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

    /** Get this [GuildEmoji] as a [standard Mention][asMention] (``<name:ID>``). */
    override fun toString(): String = asMention
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
sealed class UnicodeEmoji(
    val unicode: String,
    val tone: SkinTone? = null
) : Emoji() {
    /** The emoji's [unicode] combined with its [tone]'s unicode, if any. */
    val combinedUnicode: String = tone?.let { unicode + it.unicode } ?: unicode

    override val requestData = combinedUnicode
    override val uriData = combinedUnicode.encodeURLQueryComponent()

    /** Unicode emoji with a Discord shortcode of `:grinning:`. Represented in Unicode as 😀. */
    object Grinning : UnicodeEmoji("\ud83d\ude00")

    /** Unicode emoji with a Discord shortcode of `:smiley:`. Represented in Unicode as 😃. */
    object Smiley : UnicodeEmoji("\ud83d\ude03")

    /** Unicode emoji with a Discord shortcode of `:smile:`. Represented in Unicode as 😄. */
    object Smile : UnicodeEmoji("\ud83d\ude04")

    /** Unicode emoji with a Discord shortcode of `:grin:`. Represented in Unicode as 😁. */
    object Grin : UnicodeEmoji("\ud83d\ude01")

    /** Unicode emoji with Discord shortcodes of `:laughing:` and `:satisfied:`. Represented in Unicode as 😆. */
    object Laughing : UnicodeEmoji("\ud83d\ude06")

    /** Unicode emoji with a Discord shortcode of `:sweat_smile:`. Represented in Unicode as 😅. */
    object SweatSmile : UnicodeEmoji("\ud83d\ude05")

    /** Unicode emoji with a Discord shortcode of `:joy:`. Represented in Unicode as 😂. */
    object Joy : UnicodeEmoji("\ud83d\ude02")

    /** Unicode emoji with Discord shortcodes of `:rofl:` and `:rolling_on_the_floor_laughing:`. Represented in Unicode as 🤣. */
    object Rofl : UnicodeEmoji("\ud83e\udd23")

    /** Unicode emoji with a Discord shortcode of `:relaxed:`. Represented in Unicode as ☺️. */
    object Relaxed : UnicodeEmoji("\u263a\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:blush:`. Represented in Unicode as 😊. */
    object Blush : UnicodeEmoji("\ud83d\ude0a")

    /** Unicode emoji with a Discord shortcode of `:innocent:`. Represented in Unicode as 😇. */
    object Innocent : UnicodeEmoji("\ud83d\ude07")

    /** Unicode emoji with Discord shortcodes of `:slight_smile:` and `:slightly_smiling_face:`. Represented in Unicode as 🙂. */
    object SlightSmile : UnicodeEmoji("\ud83d\ude42")

    /** Unicode emoji with Discord shortcodes of `:upside_down:` and `:upside_down_face:`. Represented in Unicode as 🙃. */
    object UpsideDown : UnicodeEmoji("\ud83d\ude43")

    /** Unicode emoji with a Discord shortcode of `:wink:`. Represented in Unicode as 😉. */
    object Wink : UnicodeEmoji("\ud83d\ude09")

    /** Unicode emoji with a Discord shortcode of `:relieved:`. Represented in Unicode as 😌. */
    object Relieved : UnicodeEmoji("\ud83d\ude0c")

    /** Unicode emoji with a Discord shortcode of `:heart_eyes:`. Represented in Unicode as 😍. */
    object HeartEyes : UnicodeEmoji("\ud83d\ude0d")

    /** Unicode emoji with a Discord shortcode of `:smiling_face_with_3_hearts:`. Represented in Unicode as 🥰. */
    object SmilingFaceWith3Hearts : UnicodeEmoji("\ud83e\udd70")

    /** Unicode emoji with a Discord shortcode of `:kissing_heart:`. Represented in Unicode as 😘. */
    object KissingHeart : UnicodeEmoji("\ud83d\ude18")

    /** Unicode emoji with a Discord shortcode of `:kissing:`. Represented in Unicode as 😗. */
    object Kissing : UnicodeEmoji("\ud83d\ude17")

    /** Unicode emoji with a Discord shortcode of `:kissing_smiling_eyes:`. Represented in Unicode as 😙. */
    object KissingSmilingEyes : UnicodeEmoji("\ud83d\ude19")

    /** Unicode emoji with a Discord shortcode of `:kissing_closed_eyes:`. Represented in Unicode as 😚. */
    object KissingClosedEyes : UnicodeEmoji("\ud83d\ude1a")

    /** Unicode emoji with a Discord shortcode of `:yum:`. Represented in Unicode as 😋. */
    object Yum : UnicodeEmoji("\ud83d\ude0b")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue:`. Represented in Unicode as 😛. */
    object StuckOutTongue : UnicodeEmoji("\ud83d\ude1b")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue_closed_eyes:`. Represented in Unicode as 😝. */
    object StuckOutTongueClosedEyes : UnicodeEmoji("\ud83d\ude1d")

    /** Unicode emoji with a Discord shortcode of `:stuck_out_tongue_winking_eye:`. Represented in Unicode as 😜. */
    object StuckOutTongueWinkingEye : UnicodeEmoji("\ud83d\ude1c")

    /** Unicode emoji with a Discord shortcode of `:zany_face:`. Represented in Unicode as 🤪. */
    object ZanyFace : UnicodeEmoji("\ud83e\udd2a")

    /** Unicode emoji with a Discord shortcode of `:face_with_raised_eyebrow:`. Represented in Unicode as 🤨. */
    object FaceWithRaisedEyebrow : UnicodeEmoji("\ud83e\udd28")

    /** Unicode emoji with a Discord shortcode of `:face_with_monocle:`. Represented in Unicode as 🧐. */
    object FaceWithMonocle : UnicodeEmoji("\ud83e\uddd0")

    /** Unicode emoji with Discord shortcodes of `:nerd:` and `:nerd_face:`. Represented in Unicode as 🤓. */
    object Nerd : UnicodeEmoji("\ud83e\udd13")

    /** Unicode emoji with a Discord shortcode of `:sunglasses:`. Represented in Unicode as 😎. */
    object Sunglasses : UnicodeEmoji("\ud83d\ude0e")

    /** Unicode emoji with a Discord shortcode of `:star_struck:`. Represented in Unicode as 🤩. */
    object StarStruck : UnicodeEmoji("\ud83e\udd29")

    /** Unicode emoji with a Discord shortcode of `:partying_face:`. Represented in Unicode as 🥳. */
    object PartyingFace : UnicodeEmoji("\ud83e\udd73")

    /** Unicode emoji with a Discord shortcode of `:smirk:`. Represented in Unicode as 😏. */
    object Smirk : UnicodeEmoji("\ud83d\ude0f")

    /** Unicode emoji with a Discord shortcode of `:unamused:`. Represented in Unicode as 😒. */
    object Unamused : UnicodeEmoji("\ud83d\ude12")

    /** Unicode emoji with a Discord shortcode of `:disappointed:`. Represented in Unicode as 😞. */
    object Disappointed : UnicodeEmoji("\ud83d\ude1e")

    /** Unicode emoji with a Discord shortcode of `:pensive:`. Represented in Unicode as 😔. */
    object Pensive : UnicodeEmoji("\ud83d\ude14")

    /** Unicode emoji with a Discord shortcode of `:worried:`. Represented in Unicode as 😟. */
    object Worried : UnicodeEmoji("\ud83d\ude1f")

    /** Unicode emoji with a Discord shortcode of `:confused:`. Represented in Unicode as 😕. */
    object Confused : UnicodeEmoji("\ud83d\ude15")

    /** Unicode emoji with Discord shortcodes of `:slight_frown:` and `:slightly_frowning_face:`. Represented in Unicode as 🙁. */
    object SlightFrown : UnicodeEmoji("\ud83d\ude41")

    /** Unicode emoji with Discord shortcodes of `:frowning2:` and `:white_frowning_face:`. Represented in Unicode as ☹️. */
    object Frowning2 : UnicodeEmoji("\u2639\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:persevere:`. Represented in Unicode as 😣. */
    object Persevere : UnicodeEmoji("\ud83d\ude23")

    /** Unicode emoji with a Discord shortcode of `:confounded:`. Represented in Unicode as 😖. */
    object Confounded : UnicodeEmoji("\ud83d\ude16")

    /** Unicode emoji with a Discord shortcode of `:tired_face:`. Represented in Unicode as 😫. */
    object TiredFace : UnicodeEmoji("\ud83d\ude2b")

    /** Unicode emoji with a Discord shortcode of `:weary:`. Represented in Unicode as 😩. */
    object Weary : UnicodeEmoji("\ud83d\ude29")

    /** Unicode emoji with a Discord shortcode of `:pleading_face:`. Represented in Unicode as 🥺. */
    object PleadingFace : UnicodeEmoji("\ud83e\udd7a")

    /** Unicode emoji with a Discord shortcode of `:cry:`. Represented in Unicode as 😢. */
    object Cry : UnicodeEmoji("\ud83d\ude22")

    /** Unicode emoji with a Discord shortcode of `:sob:`. Represented in Unicode as 😭. */
    object Sob : UnicodeEmoji("\ud83d\ude2d")

    /** Unicode emoji with a Discord shortcode of `:triumph:`. Represented in Unicode as 😤. */
    object Triumph : UnicodeEmoji("\ud83d\ude24")

    /** Unicode emoji with a Discord shortcode of `:angry:`. Represented in Unicode as 😠. */
    object Angry : UnicodeEmoji("\ud83d\ude20")

    /** Unicode emoji with a Discord shortcode of `:rage:`. Represented in Unicode as 😡. */
    object Rage : UnicodeEmoji("\ud83d\ude21")

    /** Unicode emoji with a Discord shortcode of `:face_with_symbols_over_mouth:`. Represented in Unicode as 🤬. */
    object FaceWithSymbolsOverMouth : UnicodeEmoji("\ud83e\udd2c")

    /** Unicode emoji with a Discord shortcode of `:exploding_head:`. Represented in Unicode as 🤯. */
    object ExplodingHead : UnicodeEmoji("\ud83e\udd2f")

    /** Unicode emoji with a Discord shortcode of `:flushed:`. Represented in Unicode as 😳. */
    object Flushed : UnicodeEmoji("\ud83d\ude33")

    /** Unicode emoji with a Discord shortcode of `:hot_face:`. Represented in Unicode as 🥵. */
    object HotFace : UnicodeEmoji("\ud83e\udd75")

    /** Unicode emoji with a Discord shortcode of `:cold_face:`. Represented in Unicode as 🥶. */
    object ColdFace : UnicodeEmoji("\ud83e\udd76")

    /** Unicode emoji with a Discord shortcode of `:scream:`. Represented in Unicode as 😱. */
    object Scream : UnicodeEmoji("\ud83d\ude31")

    /** Unicode emoji with a Discord shortcode of `:fearful:`. Represented in Unicode as 😨. */
    object Fearful : UnicodeEmoji("\ud83d\ude28")

    /** Unicode emoji with a Discord shortcode of `:cold_sweat:`. Represented in Unicode as 😰. */
    object ColdSweat : UnicodeEmoji("\ud83d\ude30")

    /** Unicode emoji with a Discord shortcode of `:disappointed_relieved:`. Represented in Unicode as 😥. */
    object DisappointedRelieved : UnicodeEmoji("\ud83d\ude25")

    /** Unicode emoji with a Discord shortcode of `:sweat:`. Represented in Unicode as 😓. */
    object Sweat : UnicodeEmoji("\ud83d\ude13")

    /** Unicode emoji with Discord shortcodes of `:hugging:` and `:hugging_face:`. Represented in Unicode as 🤗. */
    object Hugging : UnicodeEmoji("\ud83e\udd17")

    /** Unicode emoji with Discord shortcodes of `:thinking:` and `:thinking_face:`. Represented in Unicode as 🤔. */
    object Thinking : UnicodeEmoji("\ud83e\udd14")

    /** Unicode emoji with a Discord shortcode of `:face_with_hand_over_mouth:`. Represented in Unicode as 🤭. */
    object FaceWithHandOverMouth : UnicodeEmoji("\ud83e\udd2d")

    /** Unicode emoji with a Discord shortcode of `:yawning_face:`. Represented in Unicode as 🥱. */
    object YawningFace : UnicodeEmoji("\ud83e\udd71")

    /** Unicode emoji with a Discord shortcode of `:shushing_face:`. Represented in Unicode as 🤫. */
    object ShushingFace : UnicodeEmoji("\ud83e\udd2b")

    /** Unicode emoji with Discord shortcodes of `:liar:` and `:lying_face:`. Represented in Unicode as 🤥. */
    object Liar : UnicodeEmoji("\ud83e\udd25")

    /** Unicode emoji with a Discord shortcode of `:no_mouth:`. Represented in Unicode as 😶. */
    object NoMouth : UnicodeEmoji("\ud83d\ude36")

    /** Unicode emoji with a Discord shortcode of `:neutral_face:`. Represented in Unicode as 😐. */
    object NeutralFace : UnicodeEmoji("\ud83d\ude10")

    /** Unicode emoji with a Discord shortcode of `:expressionless:`. Represented in Unicode as 😑. */
    object Expressionless : UnicodeEmoji("\ud83d\ude11")

    /** Unicode emoji with a Discord shortcode of `:grimacing:`. Represented in Unicode as 😬. */
    object Grimacing : UnicodeEmoji("\ud83d\ude2c")

    /** Unicode emoji with Discord shortcodes of `:rolling_eyes:` and `:face_with_rolling_eyes:`. Represented in Unicode as 🙄. */
    object RollingEyes : UnicodeEmoji("\ud83d\ude44")

    /** Unicode emoji with a Discord shortcode of `:hushed:`. Represented in Unicode as 😯. */
    object Hushed : UnicodeEmoji("\ud83d\ude2f")

    /** Unicode emoji with a Discord shortcode of `:frowning:`. Represented in Unicode as 😦. */
    object Frowning : UnicodeEmoji("\ud83d\ude26")

    /** Unicode emoji with a Discord shortcode of `:anguished:`. Represented in Unicode as 😧. */
    object Anguished : UnicodeEmoji("\ud83d\ude27")

    /** Unicode emoji with a Discord shortcode of `:open_mouth:`. Represented in Unicode as 😮. */
    object OpenMouth : UnicodeEmoji("\ud83d\ude2e")

    /** Unicode emoji with a Discord shortcode of `:astonished:`. Represented in Unicode as 😲. */
    object Astonished : UnicodeEmoji("\ud83d\ude32")

    /** Unicode emoji with a Discord shortcode of `:sleeping:`. Represented in Unicode as 😴. */
    object Sleeping : UnicodeEmoji("\ud83d\ude34")

    /** Unicode emoji with Discord shortcodes of `:drool:` and `:drooling_face:`. Represented in Unicode as 🤤. */
    object Drool : UnicodeEmoji("\ud83e\udd24")

    /** Unicode emoji with a Discord shortcode of `:sleepy:`. Represented in Unicode as 😪. */
    object Sleepy : UnicodeEmoji("\ud83d\ude2a")

    /** Unicode emoji with a Discord shortcode of `:dizzy_face:`. Represented in Unicode as 😵. */
    object DizzyFace : UnicodeEmoji("\ud83d\ude35")

    /** Unicode emoji with Discord shortcodes of `:zipper_mouth:` and `:zipper_mouth_face:`. Represented in Unicode as 🤐. */
    object ZipperMouth : UnicodeEmoji("\ud83e\udd10")

    /** Unicode emoji with a Discord shortcode of `:woozy_face:`. Represented in Unicode as 🥴. */
    object WoozyFace : UnicodeEmoji("\ud83e\udd74")

    /** Unicode emoji with Discord shortcodes of `:sick:` and `:nauseated_face:`. Represented in Unicode as 🤢. */
    object Sick : UnicodeEmoji("\ud83e\udd22")

    /** Unicode emoji with a Discord shortcode of `:face_vomiting:`. Represented in Unicode as 🤮. */
    object FaceVomiting : UnicodeEmoji("\ud83e\udd2e")

    /** Unicode emoji with Discord shortcodes of `:sneeze:` and `:sneezing_face:`. Represented in Unicode as 🤧. */
    object Sneeze : UnicodeEmoji("\ud83e\udd27")

    /** Unicode emoji with a Discord shortcode of `:mask:`. Represented in Unicode as 😷. */
    object Mask : UnicodeEmoji("\ud83d\ude37")

    /** Unicode emoji with Discord shortcodes of `:thermometer_face:` and `:face_with_thermometer:`. Represented in Unicode as 🤒. */
    object ThermometerFace : UnicodeEmoji("\ud83e\udd12")

    /** Unicode emoji with Discord shortcodes of `:head_bandage:` and `:face_with_head_bandage:`. Represented in Unicode as 🤕. */
    object HeadBandage : UnicodeEmoji("\ud83e\udd15")

    /** Unicode emoji with Discord shortcodes of `:money_mouth:` and `:money_mouth_face:`. Represented in Unicode as 🤑. */
    object MoneyMouth : UnicodeEmoji("\ud83e\udd11")

    /** Unicode emoji with Discord shortcodes of `:cowboy:` and `:face_with_cowboy_hat:`. Represented in Unicode as 🤠. */
    object Cowboy : UnicodeEmoji("\ud83e\udd20")

    /** Unicode emoji with a Discord shortcode of `:smiling_imp:`. Represented in Unicode as 😈. */
    object SmilingImp : UnicodeEmoji("\ud83d\ude08")

    /** Unicode emoji with a Discord shortcode of `:imp:`. Represented in Unicode as 👿. */
    object Imp : UnicodeEmoji("\ud83d\udc7f")

    /** Unicode emoji with a Discord shortcode of `:japanese_ogre:`. Represented in Unicode as 👹. */
    object JapaneseOgre : UnicodeEmoji("\ud83d\udc79")

    /** Unicode emoji with a Discord shortcode of `:japanese_goblin:`. Represented in Unicode as 👺. */
    object JapaneseGoblin : UnicodeEmoji("\ud83d\udc7a")

    /** Unicode emoji with Discord shortcodes of `:clown:` and `:clown_face:`. Represented in Unicode as 🤡. */
    object Clown : UnicodeEmoji("\ud83e\udd21")

    /** Unicode emoji with Discord shortcodes of `:poo:`, `:poop:`, `:shit:`, and `:hankey:`. Represented in Unicode as 💩. */
    object Poo : UnicodeEmoji("\ud83d\udca9")

    /** Unicode emoji with a Discord shortcode of `:ghost:`. Represented in Unicode as 👻. */
    object Ghost : UnicodeEmoji("\ud83d\udc7b")

    /** Unicode emoji with Discord shortcodes of `:skull:` and `:skeleton:`. Represented in Unicode as 💀. */
    object Skull : UnicodeEmoji("\ud83d\udc80")

    /** Unicode emoji with Discord shortcodes of `:skull_crossbones:` and `:skull_and_crossbones:`. Represented in Unicode as ☠️. */
    object SkullCrossbones : UnicodeEmoji("\u2620\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:alien:`. Represented in Unicode as 👽. */
    object Alien : UnicodeEmoji("\ud83d\udc7d")

    /** Unicode emoji with a Discord shortcode of `:space_invader:`. Represented in Unicode as 👾. */
    object SpaceInvader : UnicodeEmoji("\ud83d\udc7e")

    /** Unicode emoji with Discord shortcodes of `:robot:` and `:robot_face:`. Represented in Unicode as 🤖. */
    object Robot : UnicodeEmoji("\ud83e\udd16")

    /** Unicode emoji with a Discord shortcode of `:jack_o_lantern:`. Represented in Unicode as 🎃. */
    object JackOLantern : UnicodeEmoji("\ud83c\udf83")

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

    /** Unicode emoji with a Discord shortcode of `:palms_up_together:`, and the given skin [tone]. Represented in Unicode as 🤲. */
    class PalmsUpTogether(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd32", tone)

    /** Unicode emoji with a Discord shortcode of `:open_hands:`, and the given skin [tone]. Represented in Unicode as 👐. */
    class OpenHands(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc50", tone)

    /** Unicode emoji with a Discord shortcode of `:raised_hands:`, and the given skin [tone]. Represented in Unicode as 🙌. */
    class RaisedHands(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4c", tone)

    /** Unicode emoji with a Discord shortcode of `:clap:`, and the given skin [tone]. Represented in Unicode as 👏. */
    class Clap(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4f", tone)

    /** Unicode emoji with Discord shortcodes of `:handshake:` and `:shaking_hands:`. Represented in Unicode as 🤝. */
    object Handshake : UnicodeEmoji("\ud83e\udd1d")

    /** Unicode emoji with Discord shortcodes of `:+1:`, `:thumbup:`, and `:thumbsup:`, and the given skin [tone]. Represented in Unicode as 👍. */
    class ThumbsUp(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4d", tone)

    /** Unicode emoji with Discord shortcodes of `:-1:`, `:thumbdown:`, and `:thumbsdown:`, and the given skin [tone]. Represented in Unicode as 👎. */
    class ThumbsDown(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4e", tone)

    /** Unicode emoji with a Discord shortcode of `:punch:`, and the given skin [tone]. Represented in Unicode as 👊. */
    class Punch(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4a", tone)

    /** Unicode emoji with a Discord shortcode of `:fist:`, and the given skin [tone]. Represented in Unicode as ✊. */
    class Fist(tone: SkinTone? = null) : UnicodeEmoji("\u270a", tone)

    /** Unicode emoji with Discord shortcodes of `:left_fist:` and `:left_facing_fist:`, and the given skin [tone]. Represented in Unicode as 🤛. */
    class LeftFist(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1b", tone)

    /** Unicode emoji with Discord shortcodes of `:right_fist:` and `:right_facing_fist:`, and the given skin [tone]. Represented in Unicode as 🤜. */
    class RightFist(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1c", tone)

    /** Unicode emoji with Discord shortcodes of `:fingers_crossed:` and `:hand_with_index_and_middle_finger_crossed:`, and the given skin [tone]. Represented in Unicode as 🤞. */
    class FingersCrossed(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1e", tone)

    /** Unicode emoji with a Discord shortcode of `:v:`, and the given skin [tone]. Represented in Unicode as ✌️. */
    class V(tone: SkinTone? = null) : UnicodeEmoji("\u270c\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:love_you_gesture:`, and the given skin [tone]. Represented in Unicode as 🤟. */
    class LoveYouGesture(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1f", tone)

    /** Unicode emoji with Discord shortcodes of `:metal:` and `:sign_of_the_horns:`, and the given skin [tone]. Represented in Unicode as 🤘. */
    class Metal(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd18", tone)

    /** Unicode emoji with a Discord shortcode of `:ok_hand:`, and the given skin [tone]. Represented in Unicode as 👌. */
    class OkHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4c", tone)

    /** Unicode emoji with a Discord shortcode of `:pinching_hand:`, and the given skin [tone]. Represented in Unicode as 🤏. */
    class PinchingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd0f", tone)

    /** Unicode emoji with a Discord shortcode of `:point_left:`, and the given skin [tone]. Represented in Unicode as 👈. */
    class PointLeft(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc48", tone)

    /** Unicode emoji with a Discord shortcode of `:point_right:`, and the given skin [tone]. Represented in Unicode as 👉. */
    class PointRight(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc49", tone)

    /** Unicode emoji with a Discord shortcode of `:point_up_2:`, and the given skin [tone]. Represented in Unicode as 👆. */
    class PointUp2(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc46", tone)

    /** Unicode emoji with a Discord shortcode of `:point_down:`, and the given skin [tone]. Represented in Unicode as 👇. */
    class PointDown(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc47", tone)

    /** Unicode emoji with a Discord shortcode of `:point_up:`, and the given skin [tone]. Represented in Unicode as ☝️. */
    class PointUp(tone: SkinTone? = null) : UnicodeEmoji("\u261d\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:raised_hand:`, and the given skin [tone]. Represented in Unicode as ✋. */
    class RaisedHand(tone: SkinTone? = null) : UnicodeEmoji("\u270b", tone)

    /** Unicode emoji with Discord shortcodes of `:back_of_hand:` and `:raised_back_of_hand:`, and the given skin [tone]. Represented in Unicode as 🤚. */
    class BackOfHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd1a", tone)

    /** Unicode emoji with Discord shortcodes of `:hand_splayed:` and `:raised_hand_with_fingers_splayed:`, and the given skin [tone]. Represented in Unicode as 🖐️. */
    class HandSplayed(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd90\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:vulcan:` and `:raised_hand_with_part_between_middle_and_ring_fingers:`, and the given skin [tone]. Represented in Unicode as 🖖. */
    class Vulcan(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd96", tone)

    /** Unicode emoji with a Discord shortcode of `:wave:`, and the given skin [tone]. Represented in Unicode as 👋. */
    class Wave(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc4b", tone)

    /** Unicode emoji with Discord shortcodes of `:call_me:` and `:call_me_hand:`, and the given skin [tone]. Represented in Unicode as 🤙. */
    class CallMe(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd19", tone)

    /** Unicode emoji with a Discord shortcode of `:muscle:`, and the given skin [tone]. Represented in Unicode as 💪. */
    class Muscle(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udcaa", tone)

    /** Unicode emoji with a Discord shortcode of `:mechanical_arm:`. Represented in Unicode as 🦾. */
    object MechanicalArm : UnicodeEmoji("\ud83e\uddbe")

    /** Unicode emoji with Discord shortcodes of `:middle_finger:` and `:reversed_hand_with_middle_finger_extended:`, and the given skin [tone]. Represented in Unicode as 🖕. */
    class MiddleFinger(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd95", tone)

    /** Unicode emoji with a Discord shortcode of `:writing_hand:`, and the given skin [tone]. Represented in Unicode as ✍️. */
    class WritingHand(tone: SkinTone? = null) : UnicodeEmoji("\u270d\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:pray:`, and the given skin [tone]. Represented in Unicode as 🙏. */
    class Pray(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4f", tone)

    /** Unicode emoji with a Discord shortcode of `:foot:`, and the given skin [tone]. Represented in Unicode as 🦶. */
    class Foot(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb6", tone)

    /** Unicode emoji with a Discord shortcode of `:leg:`, and the given skin [tone]. Represented in Unicode as 🦵. */
    class Leg(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb5", tone)

    /** Unicode emoji with a Discord shortcode of `:mechanical_leg:`. Represented in Unicode as 🦿. */
    object MechanicalLeg : UnicodeEmoji("\ud83e\uddbf")

    /** Unicode emoji with a Discord shortcode of `:lipstick:`. Represented in Unicode as 💄. */
    object Lipstick : UnicodeEmoji("\ud83d\udc84")

    /** Unicode emoji with a Discord shortcode of `:kiss:`. Represented in Unicode as 💋. */
    object Kiss : UnicodeEmoji("\ud83d\udc8b")

    /** Unicode emoji with a Discord shortcode of `:lips:`. Represented in Unicode as 👄. */
    object Lips : UnicodeEmoji("\ud83d\udc44")

    /** Unicode emoji with a Discord shortcode of `:tooth:`. Represented in Unicode as 🦷. */
    object Tooth : UnicodeEmoji("\ud83e\uddb7")

    /** Unicode emoji with a Discord shortcode of `:bone:`. Represented in Unicode as 🦴. */
    object Bone : UnicodeEmoji("\ud83e\uddb4")

    /** Unicode emoji with a Discord shortcode of `:tongue:`. Represented in Unicode as 👅. */
    object Tongue : UnicodeEmoji("\ud83d\udc45")

    /** Unicode emoji with a Discord shortcode of `:ear:`, and the given skin [tone]. Represented in Unicode as 👂. */
    class Ear(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc42", tone)

    /** Unicode emoji with a Discord shortcode of `:ear_with_hearing_aid:`, and the given skin [tone]. Represented in Unicode as 🦻. */
    class EarWithHearingAid(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddbb", tone)

    /** Unicode emoji with a Discord shortcode of `:nose:`, and the given skin [tone]. Represented in Unicode as 👃. */
    class Nose(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc43", tone)

    /** Unicode emoji with a Discord shortcode of `:footprints:`. Represented in Unicode as 👣. */
    object Footprints : UnicodeEmoji("\ud83d\udc63")

    /** Unicode emoji with a Discord shortcode of `:eye:`. Represented in Unicode as 👁️. */
    object Eye : UnicodeEmoji("\ud83d\udc41\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:eyes:`. Represented in Unicode as 👀. */
    object Eyes : UnicodeEmoji("\ud83d\udc40")

    /** Unicode emoji with a Discord shortcode of `:brain:`. Represented in Unicode as 🧠. */
    object Brain : UnicodeEmoji("\ud83e\udde0")

    /** Unicode emoji with Discord shortcodes of `:speaking_head:` and `:speaking_head_in_silhouette:`. Represented in Unicode as 🗣️. */
    object SpeakingHead : UnicodeEmoji("\ud83d\udde3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:bust_in_silhouette:`. Represented in Unicode as 👤. */
    object BustInSilhouette : UnicodeEmoji("\ud83d\udc64")

    /** Unicode emoji with a Discord shortcode of `:busts_in_silhouette:`. Represented in Unicode as 👥. */
    object BustsInSilhouette : UnicodeEmoji("\ud83d\udc65")

    /** Unicode emoji with a Discord shortcode of `:baby:`, and the given skin [tone]. Represented in Unicode as 👶. */
    class Baby(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc76", tone)

    /** Unicode emoji with a Discord shortcode of `:girl:`, and the given skin [tone]. Represented in Unicode as 👧. */
    class Girl(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc67", tone)

    /** Unicode emoji with a Discord shortcode of `:child:`, and the given skin [tone]. Represented in Unicode as 🧒. */
    class Child(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd2", tone)

    /** Unicode emoji with a Discord shortcode of `:boy:`, and the given skin [tone]. Represented in Unicode as 👦. */
    class Boy(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc66", tone)

    /** Unicode emoji with a Discord shortcode of `:woman:`, and the given skin [tone]. Represented in Unicode as 👩. */
    class Woman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69", tone)

    /** Unicode emoji with a Discord shortcode of `:adult:`, and the given skin [tone]. Represented in Unicode as 🧑. */
    class Adult(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd1", tone)

    /** Unicode emoji with a Discord shortcode of `:man:`, and the given skin [tone]. Represented in Unicode as 👨. */
    class Man(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_curly_haired:`, and the given skin [tone]. Represented in Unicode as 👩‍🦱. */
    class WomanCurlyHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddb1", tone)

    /** Unicode emoji with a Discord shortcode of `:man_curly_haired:`, and the given skin [tone]. Represented in Unicode as 👨‍🦱. */
    class ManCurlyHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddb1", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_red_haired:`, and the given skin [tone]. Represented in Unicode as 👩‍🦰. */
    class WomanRedHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddb0", tone)

    /** Unicode emoji with a Discord shortcode of `:man_red_haired:`, and the given skin [tone]. Represented in Unicode as 👨‍🦰. */
    class ManRedHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddb0", tone)

    /** Unicode emoji with a Discord shortcode of `:blond_haired_woman:`, and the given skin [tone]. Represented in Unicode as 👱‍♀️. */
    class BlondHairedWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc71\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:blond_haired_person:` and `:person_with_blond_hair:`, and the given skin [tone]. Represented in Unicode as 👱. */
    class BlondHairedPerson(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc71", tone)

    /** Unicode emoji with a Discord shortcode of `:blond_haired_man:`, and the given skin [tone]. Represented in Unicode as 👱‍♂️. */
    class BlondHairedMan(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc71\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_white_haired:`, and the given skin [tone]. Represented in Unicode as 👩‍🦳. */
    class WomanWhiteHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddb3", tone)

    /** Unicode emoji with a Discord shortcode of `:man_white_haired:`, and the given skin [tone]. Represented in Unicode as 👨‍🦳. */
    class ManWhiteHaired(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddb3", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_bald:`, and the given skin [tone]. Represented in Unicode as 👩‍🦲. */
    class WomanBald(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddb2", tone)

    /** Unicode emoji with a Discord shortcode of `:man_bald:`, and the given skin [tone]. Represented in Unicode as 👨‍🦲. */
    class ManBald(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddb2", tone)

    /** Unicode emoji with a Discord shortcode of `:bearded_person:`, and the given skin [tone]. Represented in Unicode as 🧔. */
    class BeardedPerson(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd4", tone)

    /** Unicode emoji with Discord shortcodes of `:grandma:` and `:older_woman:`, and the given skin [tone]. Represented in Unicode as 👵. */
    class Grandma(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc75", tone)

    /** Unicode emoji with a Discord shortcode of `:older_adult:`, and the given skin [tone]. Represented in Unicode as 🧓. */
    class OlderAdult(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd3", tone)

    /** Unicode emoji with a Discord shortcode of `:older_man:`, and the given skin [tone]. Represented in Unicode as 👴. */
    class OlderMan(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc74", tone)

    /** Unicode emoji with Discord shortcodes of `:man_with_gua_pi_mao:` and `:man_with_chinese_cap:`, and the given skin [tone]. Represented in Unicode as 👲. */
    class ManWithGuaPiMao(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc72", tone)

    /** Unicode emoji with Discord shortcodes of `:man_with_turban:` and `:person_wearing_turban:`, and the given skin [tone]. Represented in Unicode as 👳. */
    class ManWithTurban(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc73", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_wearing_turban:`, and the given skin [tone]. Represented in Unicode as 👳‍♀️. */
    class WomanWearingTurban(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc73\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_wearing_turban:`, and the given skin [tone]. Represented in Unicode as 👳‍♂️. */
    class ManWearingTurban(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc73\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_with_headscarf:`, and the given skin [tone]. Represented in Unicode as 🧕. */
    class WomanWithHeadscarf(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd5", tone)

    /** Unicode emoji with Discord shortcodes of `:cop:` and `:police_officer:`, and the given skin [tone]. Represented in Unicode as 👮. */
    class Cop(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc6e", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_police_officer:`, and the given skin [tone]. Represented in Unicode as 👮‍♀️. */
    class WomanPoliceOfficer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc6e\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_police_officer:`, and the given skin [tone]. Represented in Unicode as 👮‍♂️. */
    class ManPoliceOfficer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc6e\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:construction_worker:`, and the given skin [tone]. Represented in Unicode as 👷. */
    class ConstructionWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc77", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_construction_worker:`, and the given skin [tone]. Represented in Unicode as 👷‍♀️. */
    class WomanConstructionWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc77\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_construction_worker:`, and the given skin [tone]. Represented in Unicode as 👷‍♂️. */
    class ManConstructionWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc77\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:guard:` and `:guardsman:`, and the given skin [tone]. Represented in Unicode as 💂. */
    class Guard(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc82", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_guard:`, and the given skin [tone]. Represented in Unicode as 💂‍♀️. */
    class WomanGuard(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc82\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_guard:`, and the given skin [tone]. Represented in Unicode as 💂‍♂️. */
    class ManGuard(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc82\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:spy:`, `:detective:`, and `:sleuth_or_spy:`, and the given skin [tone]. Represented in Unicode as 🕵️. */
    class Spy(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd75\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_detective:`, and the given skin [tone]. Represented in Unicode as 🕵️‍♀️. */
    class WomanDetective(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd75\ufe0f\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_detective:`, and the given skin [tone]. Represented in Unicode as 🕵️‍♂️. */
    class ManDetective(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd75\ufe0f\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_health_worker:`, and the given skin [tone]. Represented in Unicode as 👩‍⚕️. */
    class WomanHealthWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\u2695\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_health_worker:`, and the given skin [tone]. Represented in Unicode as 👨‍⚕️. */
    class ManHealthWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\u2695\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_farmer:`, and the given skin [tone]. Represented in Unicode as 👩‍🌾. */
    class WomanFarmer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udf3e", tone)

    /** Unicode emoji with a Discord shortcode of `:man_farmer:`, and the given skin [tone]. Represented in Unicode as 👨‍🌾. */
    class ManFarmer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udf3e", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_cook:`, and the given skin [tone]. Represented in Unicode as 👩‍🍳. */
    class WomanCook(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udf73", tone)

    /** Unicode emoji with a Discord shortcode of `:man_cook:`, and the given skin [tone]. Represented in Unicode as 👨‍🍳. */
    class ManCook(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udf73", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_student:`, and the given skin [tone]. Represented in Unicode as 👩‍🎓. */
    class WomanStudent(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udf93", tone)

    /** Unicode emoji with a Discord shortcode of `:man_student:`, and the given skin [tone]. Represented in Unicode as 👨‍🎓. */
    class ManStudent(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udf93", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_singer:`, and the given skin [tone]. Represented in Unicode as 👩‍🎤. */
    class WomanSinger(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udfa4", tone)

    /** Unicode emoji with a Discord shortcode of `:man_singer:`, and the given skin [tone]. Represented in Unicode as 👨‍🎤. */
    class ManSinger(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udfa4", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_teacher:`, and the given skin [tone]. Represented in Unicode as 👩‍🏫. */
    class WomanTeacher(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udfeb", tone)

    /** Unicode emoji with a Discord shortcode of `:man_teacher:`, and the given skin [tone]. Represented in Unicode as 👨‍🏫. */
    class ManTeacher(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udfeb", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_factory_worker:`, and the given skin [tone]. Represented in Unicode as 👩‍🏭. */
    class WomanFactoryWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udfed", tone)

    /** Unicode emoji with a Discord shortcode of `:man_factory_worker:`, and the given skin [tone]. Represented in Unicode as 👨‍🏭. */
    class ManFactoryWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udfed", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_technologist:`, and the given skin [tone]. Represented in Unicode as 👩‍💻. */
    class WomanTechnologist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udcbb", tone)

    /** Unicode emoji with a Discord shortcode of `:man_technologist:`, and the given skin [tone]. Represented in Unicode as 👨‍💻. */
    class ManTechnologist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udcbb", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_office_worker:`, and the given skin [tone]. Represented in Unicode as 👩‍💼. */
    class WomanOfficeWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udcbc", tone)

    /** Unicode emoji with a Discord shortcode of `:man_office_worker:`, and the given skin [tone]. Represented in Unicode as 👨‍💼. */
    class ManOfficeWorker(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udcbc", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_mechanic:`, and the given skin [tone]. Represented in Unicode as 👩‍🔧. */
    class WomanMechanic(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udd27", tone)

    /** Unicode emoji with a Discord shortcode of `:man_mechanic:`, and the given skin [tone]. Represented in Unicode as 👨‍🔧. */
    class ManMechanic(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udd27", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_scientist:`, and the given skin [tone]. Represented in Unicode as 👩‍🔬. */
    class WomanScientist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udd2c", tone)

    /** Unicode emoji with a Discord shortcode of `:man_scientist:`, and the given skin [tone]. Represented in Unicode as 👨‍🔬. */
    class ManScientist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udd2c", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_artist:`, and the given skin [tone]. Represented in Unicode as 👩‍🎨. */
    class WomanArtist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83c\udfa8", tone)

    /** Unicode emoji with a Discord shortcode of `:man_artist:`, and the given skin [tone]. Represented in Unicode as 👨‍🎨. */
    class ManArtist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83c\udfa8", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_firefighter:`, and the given skin [tone]. Represented in Unicode as 👩‍🚒. */
    class WomanFirefighter(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\ude92", tone)

    /** Unicode emoji with a Discord shortcode of `:man_firefighter:`, and the given skin [tone]. Represented in Unicode as 👨‍🚒. */
    class ManFirefighter(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\ude92", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_pilot:`, and the given skin [tone]. Represented in Unicode as 👩‍✈️. */
    class WomanPilot(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\u2708\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_pilot:`, and the given skin [tone]. Represented in Unicode as 👨‍✈️. */
    class ManPilot(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\u2708\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_astronaut:`, and the given skin [tone]. Represented in Unicode as 👩‍🚀. */
    class WomanAstronaut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\ude80", tone)

    /** Unicode emoji with a Discord shortcode of `:man_astronaut:`, and the given skin [tone]. Represented in Unicode as 👨‍🚀. */
    class ManAstronaut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\ude80", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_judge:`, and the given skin [tone]. Represented in Unicode as 👩‍⚖️. */
    class WomanJudge(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\u2696\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_judge:`, and the given skin [tone]. Represented in Unicode as 👨‍⚖️. */
    class ManJudge(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\u2696\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:bride_with_veil:`, and the given skin [tone]. Represented in Unicode as 👰. */
    class BrideWithVeil(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc70", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_tuxedo:`, and the given skin [tone]. Represented in Unicode as 🤵. */
    class ManInTuxedo(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd35", tone)

    /** Unicode emoji with a Discord shortcode of `:princess:`, and the given skin [tone]. Represented in Unicode as 👸. */
    class Princess(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc78", tone)

    /** Unicode emoji with a Discord shortcode of `:prince:`, and the given skin [tone]. Represented in Unicode as 🤴. */
    class Prince(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd34", tone)

    /** Unicode emoji with a Discord shortcode of `:superhero:`, and the given skin [tone]. Represented in Unicode as 🦸. */
    class Superhero(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb8", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_superhero:`, and the given skin [tone]. Represented in Unicode as 🦸‍♀️. */
    class WomanSuperhero(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb8\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_superhero:`, and the given skin [tone]. Represented in Unicode as 🦸‍♂️. */
    class ManSuperhero(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb8\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:supervillain:`, and the given skin [tone]. Represented in Unicode as 🦹. */
    class Supervillain(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb9", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_supervillain:`, and the given skin [tone]. Represented in Unicode as 🦹‍♀️. */
    class WomanSupervillain(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb9\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_supervillain:`, and the given skin [tone]. Represented in Unicode as 🦹‍♂️. */
    class ManSupervillain(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddb9\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:mrs_claus:` and `:mother_christmas:`, and the given skin [tone]. Represented in Unicode as 🤶. */
    class MrsClaus(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd36", tone)

    /** Unicode emoji with a Discord shortcode of `:santa:`, and the given skin [tone]. Represented in Unicode as 🎅. */
    class Santa(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udf85", tone)

    /** Unicode emoji with a Discord shortcode of `:mage:`, and the given skin [tone]. Represented in Unicode as 🧙. */
    class Mage(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd9", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_mage:`, and the given skin [tone]. Represented in Unicode as 🧙‍♀️. */
    class WomanMage(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd9\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_mage:`, and the given skin [tone]. Represented in Unicode as 🧙‍♂️. */
    class ManMage(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd9\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:elf:`, and the given skin [tone]. Represented in Unicode as 🧝. */
    class Elf(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddd", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_elf:`, and the given skin [tone]. Represented in Unicode as 🧝‍♀️. */
    class WomanElf(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddd\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_elf:`, and the given skin [tone]. Represented in Unicode as 🧝‍♂️. */
    class ManElf(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddd\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:vampire:`, and the given skin [tone]. Represented in Unicode as 🧛. */
    class Vampire(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddb", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_vampire:`, and the given skin [tone]. Represented in Unicode as 🧛‍♀️. */
    class WomanVampire(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddb\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_vampire:`, and the given skin [tone]. Represented in Unicode as 🧛‍♂️. */
    class ManVampire(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddb\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:zombie:`. Represented in Unicode as 🧟. */
    object Zombie : UnicodeEmoji("\ud83e\udddf")

    /** Unicode emoji with a Discord shortcode of `:woman_zombie:`. Represented in Unicode as 🧟‍♀️. */
    object WomanZombie : UnicodeEmoji("\ud83e\udddf\u200d\u2640\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:man_zombie:`. Represented in Unicode as 🧟‍♂️. */
    object ManZombie : UnicodeEmoji("\ud83e\udddf\u200d\u2642\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:genie:`. Represented in Unicode as 🧞. */
    object Genie : UnicodeEmoji("\ud83e\uddde")

    /** Unicode emoji with a Discord shortcode of `:woman_genie:`. Represented in Unicode as 🧞‍♀️. */
    object WomanGenie : UnicodeEmoji("\ud83e\uddde\u200d\u2640\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:man_genie:`. Represented in Unicode as 🧞‍♂️. */
    object ManGenie : UnicodeEmoji("\ud83e\uddde\u200d\u2642\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:merperson:`, and the given skin [tone]. Represented in Unicode as 🧜. */
    class Merperson(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddc", tone)

    /** Unicode emoji with a Discord shortcode of `:mermaid:`, and the given skin [tone]. Represented in Unicode as 🧜‍♀️. */
    class Mermaid(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddc\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:merman:`, and the given skin [tone]. Represented in Unicode as 🧜‍♂️. */
    class Merman(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udddc\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:fairy:`, and the given skin [tone]. Represented in Unicode as 🧚. */
    class Fairy(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddda", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_fairy:`, and the given skin [tone]. Represented in Unicode as 🧚‍♀️. */
    class WomanFairy(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddda\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_fairy:`, and the given skin [tone]. Represented in Unicode as 🧚‍♂️. */
    class ManFairy(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddda\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:angel:`, and the given skin [tone]. Represented in Unicode as 👼. */
    class Angel(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc7c", tone)

    /** Unicode emoji with Discord shortcodes of `:pregnant_woman:` and `:expecting_woman:`, and the given skin [tone]. Represented in Unicode as 🤰. */
    class PregnantWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd30", tone)

    /** Unicode emoji with a Discord shortcode of `:breast_feeding:`, and the given skin [tone]. Represented in Unicode as 🤱. */
    class BreastFeeding(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd31", tone)

    /** Unicode emoji with Discord shortcodes of `:bow:` and `:person_bowing:`, and the given skin [tone]. Represented in Unicode as 🙇. */
    class Bow(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude47", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_bowing:`, and the given skin [tone]. Represented in Unicode as 🙇‍♀️. */
    class WomanBowing(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude47\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_bowing:`, and the given skin [tone]. Represented in Unicode as 🙇‍♂️. */
    class ManBowing(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude47\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:person_tipping_hand:` and `:information_desk_person:`, and the given skin [tone]. Represented in Unicode as 💁. */
    class PersonTippingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc81", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_tipping_hand:`, and the given skin [tone]. Represented in Unicode as 💁‍♀️. */
    class WomanTippingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc81\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_tipping_hand:`, and the given skin [tone]. Represented in Unicode as 💁‍♂️. */
    class ManTippingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc81\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:no_good:` and `:person_gesturing_no:`, and the given skin [tone]. Represented in Unicode as 🙅. */
    class NoGood(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude45", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_gesturing_no:`, and the given skin [tone]. Represented in Unicode as 🙅‍♀️. */
    class WomanGesturingNo(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude45\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_gesturing_no:`, and the given skin [tone]. Represented in Unicode as 🙅‍♂️. */
    class ManGesturingNo(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude45\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:ok_woman:` and `:person_gesturing_ok:`, and the given skin [tone]. Represented in Unicode as 🙆. */
    class OkWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude46", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_gesturing_ok:`, and the given skin [tone]. Represented in Unicode as 🙆‍♀️. */
    class WomanGesturingOk(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude46\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_gesturing_ok:`, and the given skin [tone]. Represented in Unicode as 🙆‍♂️. */
    class ManGesturingOk(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude46\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:raising_hand:` and `:person_raising_hand:`, and the given skin [tone]. Represented in Unicode as 🙋. */
    class RaisingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4b", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_raising_hand:`, and the given skin [tone]. Represented in Unicode as 🙋‍♀️. */
    class WomanRaisingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4b\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_raising_hand:`, and the given skin [tone]. Represented in Unicode as 🙋‍♂️. */
    class ManRaisingHand(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4b\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:deaf_person:`, and the given skin [tone]. Represented in Unicode as 🧏. */
    class DeafPerson(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcf", tone)

    /** Unicode emoji with a Discord shortcode of `:deaf_woman:`, and the given skin [tone]. Represented in Unicode as 🧏‍♀️. */
    class DeafWoman(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcf\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:deaf_man:`, and the given skin [tone]. Represented in Unicode as 🧏‍♂️. */
    class DeafMan(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcf\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:facepalm:`, `:face_palm:`, and `:person_facepalming:`, and the given skin [tone]. Represented in Unicode as 🤦. */
    class Facepalm(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd26", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_facepalming:`, and the given skin [tone]. Represented in Unicode as 🤦‍♀️. */
    class WomanFacepalming(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd26\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_facepalming:`, and the given skin [tone]. Represented in Unicode as 🤦‍♂️. */
    class ManFacepalming(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd26\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:shrug:` and `:person_shrugging:`, and the given skin [tone]. Represented in Unicode as 🤷. */
    class Shrug(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd37", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_shrugging:`, and the given skin [tone]. Represented in Unicode as 🤷‍♀️. */
    class WomanShrugging(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd37\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_shrugging:`, and the given skin [tone]. Represented in Unicode as 🤷‍♂️. */
    class ManShrugging(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd37\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:person_pouting:` and `:person_with_pouting_face:`, and the given skin [tone]. Represented in Unicode as 🙎. */
    class PersonPouting(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4e", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_pouting:`, and the given skin [tone]. Represented in Unicode as 🙎‍♀️. */
    class WomanPouting(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4e\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_pouting:`, and the given skin [tone]. Represented in Unicode as 🙎‍♂️. */
    class ManPouting(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4e\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:person_frowning:`, and the given skin [tone]. Represented in Unicode as 🙍. */
    class PersonFrowning(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4d", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_frowning:`, and the given skin [tone]. Represented in Unicode as 🙍‍♀️. */
    class WomanFrowning(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4d\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_frowning:`, and the given skin [tone]. Represented in Unicode as 🙍‍♂️. */
    class ManFrowning(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\ude4d\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:haircut:` and `:person_getting_haircut:`, and the given skin [tone]. Represented in Unicode as 💇. */
    class Haircut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc87", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_getting_haircut:`, and the given skin [tone]. Represented in Unicode as 💇‍♀️. */
    class WomanGettingHaircut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc87\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_getting_haircut:`, and the given skin [tone]. Represented in Unicode as 💇‍♂️. */
    class ManGettingHaircut(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc87\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:massage:` and `:person_getting_massage:`, and the given skin [tone]. Represented in Unicode as 💆. */
    class Massage(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc86", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_getting_face_massage:`, and the given skin [tone]. Represented in Unicode as 💆‍♀️. */
    class WomanGettingFaceMassage(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc86\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_getting_face_massage:`, and the given skin [tone]. Represented in Unicode as 💆‍♂️. */
    class ManGettingFaceMassage(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc86\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:person_in_steamy_room:`, and the given skin [tone]. Represented in Unicode as 🧖. */
    class PersonInSteamyRoom(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd6", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_in_steamy_room:`, and the given skin [tone]. Represented in Unicode as 🧖‍♀️. */
    class WomanInSteamyRoom(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd6\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_steamy_room:`, and the given skin [tone]. Represented in Unicode as 🧖‍♂️. */
    class ManInSteamyRoom(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd6\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:nail_care:`, and the given skin [tone]. Represented in Unicode as 💅. */
    class NailCare(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc85", tone)

    /** Unicode emoji with a Discord shortcode of `:selfie:`, and the given skin [tone]. Represented in Unicode as 🤳. */
    class Selfie(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd33", tone)

    /** Unicode emoji with a Discord shortcode of `:dancer:`, and the given skin [tone]. Represented in Unicode as 💃. */
    class Dancer(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc83", tone)

    /** Unicode emoji with Discord shortcodes of `:man_dancing:` and `:male_dancer:`, and the given skin [tone]. Represented in Unicode as 🕺. */
    class ManDancing(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd7a", tone)

    /** Unicode emoji with Discord shortcodes of `:dancers:` and `:people_with_bunny_ears_partying:`. Represented in Unicode as 👯. */
    object Dancers : UnicodeEmoji("\ud83d\udc6f")

    /** Unicode emoji with a Discord shortcode of `:women_with_bunny_ears_partying:`. Represented in Unicode as 👯‍♀️. */
    object WomenWithBunnyEarsPartying : UnicodeEmoji("\ud83d\udc6f\u200d\u2640\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:men_with_bunny_ears_partying:`. Represented in Unicode as 👯‍♂️. */
    object MenWithBunnyEarsPartying : UnicodeEmoji("\ud83d\udc6f\u200d\u2642\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:levitate:` and `:man_in_business_suit_levitating:`, and the given skin [tone]. Represented in Unicode as 🕴️. */
    class Levitate(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udd74\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:walking:` and `:person_walking:`, and the given skin [tone]. Represented in Unicode as 🚶. */
    class Walking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb6", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_walking:`, and the given skin [tone]. Represented in Unicode as 🚶‍♀️. */
    class WomanWalking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb6\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_walking:`, and the given skin [tone]. Represented in Unicode as 🚶‍♂️. */
    class ManWalking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb6\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:runner:` and `:person_running:`, and the given skin [tone]. Represented in Unicode as 🏃. */
    class Runner(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc3", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_running:`, and the given skin [tone]. Represented in Unicode as 🏃‍♀️. */
    class WomanRunning(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc3\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_running:`, and the given skin [tone]. Represented in Unicode as 🏃‍♂️. */
    class ManRunning(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc3\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:person_standing:`, and the given skin [tone]. Represented in Unicode as 🧍. */
    class PersonStanding(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcd", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_standing:`, and the given skin [tone]. Represented in Unicode as 🧍‍♀️. */
    class WomanStanding(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcd\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_standing:`, and the given skin [tone]. Represented in Unicode as 🧍‍♂️. */
    class ManStanding(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddcd\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:person_kneeling:`, and the given skin [tone]. Represented in Unicode as 🧎. */
    class PersonKneeling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddce", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_kneeling:`, and the given skin [tone]. Represented in Unicode as 🧎‍♀️. */
    class WomanKneeling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddce\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_kneeling:`, and the given skin [tone]. Represented in Unicode as 🧎‍♂️. */
    class ManKneeling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddce\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_with_probing_cane:`, and the given skin [tone]. Represented in Unicode as 👩‍🦯. */
    class WomanWithProbingCane(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddaf", tone)

    /** Unicode emoji with a Discord shortcode of `:man_with_probing_cane:`, and the given skin [tone]. Represented in Unicode as 👨‍🦯. */
    class ManWithProbingCane(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddaf", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_in_motorized_wheelchair:`, and the given skin [tone]. Represented in Unicode as 👩‍🦼. */
    class WomanInMotorizedWheelchair(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddbc", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_motorized_wheelchair:`, and the given skin [tone]. Represented in Unicode as 👨‍🦼. */
    class ManInMotorizedWheelchair(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddbc", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_in_manual_wheelchair:`, and the given skin [tone]. Represented in Unicode as 👩‍🦽. */
    class WomanInManualWheelchair(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc69\u200d\ud83e\uddbd", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_manual_wheelchair:`, and the given skin [tone]. Represented in Unicode as 👨‍🦽. */
    class ManInManualWheelchair(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udc68\u200d\ud83e\uddbd", tone)

    /** Unicode emoji with a Discord shortcode of `:people_holding_hands:`. Represented in Unicode as 🧑‍🤝‍🧑. */
    object PeopleHoldingHands : UnicodeEmoji("\ud83e\uddd1\u200d\ud83e\udd1d\u200d\ud83e\uddd1")

    /** Unicode emoji with a Discord shortcode of `:couple:`. Represented in Unicode as 👫. */
    object Couple : UnicodeEmoji("\ud83d\udc6b")

    /** Unicode emoji with a Discord shortcode of `:two_women_holding_hands:`. Represented in Unicode as 👭. */
    object TwoWomenHoldingHands : UnicodeEmoji("\ud83d\udc6d")

    /** Unicode emoji with a Discord shortcode of `:two_men_holding_hands:`. Represented in Unicode as 👬. */
    object TwoMenHoldingHands : UnicodeEmoji("\ud83d\udc6c")

    /** Unicode emoji with a Discord shortcode of `:couple_with_heart:`. Represented in Unicode as 💑. */
    object CoupleWithHeart : UnicodeEmoji("\ud83d\udc91")

    /** Unicode emoji with a Discord shortcode of `:couple_with_heart_woman_man:`. Represented in Unicode as 👩‍❤️‍👨. */
    object CoupleWithHeartWomanMan : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc68")

    /** Unicode emoji with Discord shortcodes of `:couple_ww:` and `:couple_with_heart_ww:`. Represented in Unicode as 👩‍❤️‍👩. */
    object CoupleWw : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc69")

    /** Unicode emoji with Discord shortcodes of `:couple_mm:` and `:couple_with_heart_mm:`. Represented in Unicode as 👨‍❤️‍👨. */
    object CoupleMm : UnicodeEmoji("\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc68")

    /** Unicode emoji with a Discord shortcode of `:couplekiss:`. Represented in Unicode as 💏. */
    object Couplekiss : UnicodeEmoji("\ud83d\udc8f")

    /** Unicode emoji with a Discord shortcode of `:kiss_woman_man:`. Represented in Unicode as 👩‍❤️‍💋‍👨. */
    object KissWomanMan : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68")

    /** Unicode emoji with Discord shortcodes of `:kiss_ww:` and `:couplekiss_ww:`. Represented in Unicode as 👩‍❤️‍💋‍👩. */
    object KissWw : UnicodeEmoji("\ud83d\udc69\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc69")

    /** Unicode emoji with Discord shortcodes of `:kiss_mm:` and `:couplekiss_mm:`. Represented in Unicode as 👨‍❤️‍💋‍👨. */
    object KissMm : UnicodeEmoji("\ud83d\udc68\u200d\u2764\ufe0f\u200d\ud83d\udc8b\u200d\ud83d\udc68")

    /** Unicode emoji with a Discord shortcode of `:family:`. Represented in Unicode as 👪. */
    object Family : UnicodeEmoji("\ud83d\udc6a")

    /** Unicode emoji with a Discord shortcode of `:family_man_woman_boy:`. Represented in Unicode as 👨‍👩‍👦. */
    object FamilyManWomanBoy : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc66")

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

    /** Unicode emoji with a Discord shortcode of `:family_woman_boy:`. Represented in Unicode as 👩‍👦. */
    object FamilyWomanBoy : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_woman_girl:`. Represented in Unicode as 👩‍👧. */
    object FamilyWomanGirl : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_woman_girl_boy:`. Represented in Unicode as 👩‍👧‍👦. */
    object FamilyWomanGirlBoy : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_woman_boy_boy:`. Represented in Unicode as 👩‍👦‍👦. */
    object FamilyWomanBoyBoy : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc66\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_woman_girl_girl:`. Represented in Unicode as 👩‍👧‍👧. */
    object FamilyWomanGirlGirl : UnicodeEmoji("\ud83d\udc69\u200d\ud83d\udc67\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_man_boy:`. Represented in Unicode as 👨‍👦. */
    object FamilyManBoy : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_man_girl:`. Represented in Unicode as 👨‍👧. */
    object FamilyManGirl : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:family_man_girl_boy:`. Represented in Unicode as 👨‍👧‍👦. */
    object FamilyManGirlBoy : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_man_boy_boy:`. Represented in Unicode as 👨‍👦‍👦. */
    object FamilyManBoyBoy : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc66\u200d\ud83d\udc66")

    /** Unicode emoji with a Discord shortcode of `:family_man_girl_girl:`. Represented in Unicode as 👨‍👧‍👧. */
    object FamilyManGirlGirl : UnicodeEmoji("\ud83d\udc68\u200d\ud83d\udc67\u200d\ud83d\udc67")

    /** Unicode emoji with a Discord shortcode of `:yarn:`. Represented in Unicode as 🧶. */
    object Yarn : UnicodeEmoji("\ud83e\uddf6")

    /** Unicode emoji with a Discord shortcode of `:thread:`. Represented in Unicode as 🧵. */
    object Thread : UnicodeEmoji("\ud83e\uddf5")

    /** Unicode emoji with a Discord shortcode of `:coat:`. Represented in Unicode as 🧥. */
    object Coat : UnicodeEmoji("\ud83e\udde5")

    /** Unicode emoji with a Discord shortcode of `:lab_coat:`. Represented in Unicode as 🥼. */
    object LabCoat : UnicodeEmoji("\ud83e\udd7c")

    /** Unicode emoji with a Discord shortcode of `:safety_vest:`. Represented in Unicode as 🦺. */
    object SafetyVest : UnicodeEmoji("\ud83e\uddba")

    /** Unicode emoji with a Discord shortcode of `:womans_clothes:`. Represented in Unicode as 👚. */
    object WomansClothes : UnicodeEmoji("\ud83d\udc5a")

    /** Unicode emoji with a Discord shortcode of `:shirt:`. Represented in Unicode as 👕. */
    object Shirt : UnicodeEmoji("\ud83d\udc55")

    /** Unicode emoji with a Discord shortcode of `:jeans:`. Represented in Unicode as 👖. */
    object Jeans : UnicodeEmoji("\ud83d\udc56")

    /** Unicode emoji with a Discord shortcode of `:shorts:`. Represented in Unicode as 🩳. */
    object Shorts : UnicodeEmoji("\ud83e\ude73")

    /** Unicode emoji with a Discord shortcode of `:necktie:`. Represented in Unicode as 👔. */
    object Necktie : UnicodeEmoji("\ud83d\udc54")

    /** Unicode emoji with a Discord shortcode of `:dress:`. Represented in Unicode as 👗. */
    object Dress : UnicodeEmoji("\ud83d\udc57")

    /** Unicode emoji with a Discord shortcode of `:bikini:`. Represented in Unicode as 👙. */
    object Bikini : UnicodeEmoji("\ud83d\udc59")

    /** Unicode emoji with a Discord shortcode of `:one_piece_swimsuit:`. Represented in Unicode as 🩱. */
    object OnePieceSwimsuit : UnicodeEmoji("\ud83e\ude71")

    /** Unicode emoji with a Discord shortcode of `:kimono:`. Represented in Unicode as 👘. */
    object Kimono : UnicodeEmoji("\ud83d\udc58")

    /** Unicode emoji with a Discord shortcode of `:sari:`. Represented in Unicode as 🥻. */
    object Sari : UnicodeEmoji("\ud83e\udd7b")

    /** Unicode emoji with a Discord shortcode of `:womans_flat_shoe:`. Represented in Unicode as 🥿. */
    object WomansFlatShoe : UnicodeEmoji("\ud83e\udd7f")

    /** Unicode emoji with a Discord shortcode of `:high_heel:`. Represented in Unicode as 👠. */
    object HighHeel : UnicodeEmoji("\ud83d\udc60")

    /** Unicode emoji with a Discord shortcode of `:sandal:`. Represented in Unicode as 👡. */
    object Sandal : UnicodeEmoji("\ud83d\udc61")

    /** Unicode emoji with a Discord shortcode of `:boot:`. Represented in Unicode as 👢. */
    object Boot : UnicodeEmoji("\ud83d\udc62")

    /** Unicode emoji with a Discord shortcode of `:ballet_shoes:`. Represented in Unicode as 🩰. */
    object BalletShoes : UnicodeEmoji("\ud83e\ude70")

    /** Unicode emoji with a Discord shortcode of `:mans_shoe:`. Represented in Unicode as 👞. */
    object MansShoe : UnicodeEmoji("\ud83d\udc5e")

    /** Unicode emoji with a Discord shortcode of `:athletic_shoe:`. Represented in Unicode as 👟. */
    object AthleticShoe : UnicodeEmoji("\ud83d\udc5f")

    /** Unicode emoji with a Discord shortcode of `:hiking_boot:`. Represented in Unicode as 🥾. */
    object HikingBoot : UnicodeEmoji("\ud83e\udd7e")

    /** Unicode emoji with a Discord shortcode of `:briefs:`. Represented in Unicode as 🩲. */
    object Briefs : UnicodeEmoji("\ud83e\ude72")

    /** Unicode emoji with a Discord shortcode of `:socks:`. Represented in Unicode as 🧦. */
    object Socks : UnicodeEmoji("\ud83e\udde6")

    /** Unicode emoji with a Discord shortcode of `:gloves:`. Represented in Unicode as 🧤. */
    object Gloves : UnicodeEmoji("\ud83e\udde4")

    /** Unicode emoji with a Discord shortcode of `:scarf:`. Represented in Unicode as 🧣. */
    object Scarf : UnicodeEmoji("\ud83e\udde3")

    /** Unicode emoji with a Discord shortcode of `:tophat:`. Represented in Unicode as 🎩. */
    object Tophat : UnicodeEmoji("\ud83c\udfa9")

    /** Unicode emoji with a Discord shortcode of `:billed_cap:`. Represented in Unicode as 🧢. */
    object BilledCap : UnicodeEmoji("\ud83e\udde2")

    /** Unicode emoji with a Discord shortcode of `:womans_hat:`. Represented in Unicode as 👒. */
    object WomansHat : UnicodeEmoji("\ud83d\udc52")

    /** Unicode emoji with a Discord shortcode of `:mortar_board:`. Represented in Unicode as 🎓. */
    object MortarBoard : UnicodeEmoji("\ud83c\udf93")

    /** Unicode emoji with Discord shortcodes of `:helmet_with_cross:` and `:helmet_with_white_cross:`. Represented in Unicode as ⛑️. */
    object HelmetWithCross : UnicodeEmoji("\u26d1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:crown:`. Represented in Unicode as 👑. */
    object Crown : UnicodeEmoji("\ud83d\udc51")

    /** Unicode emoji with a Discord shortcode of `:ring:`. Represented in Unicode as 💍. */
    object Ring : UnicodeEmoji("\ud83d\udc8d")

    /** Unicode emoji with a Discord shortcode of `:pouch:`. Represented in Unicode as 👝. */
    object Pouch : UnicodeEmoji("\ud83d\udc5d")

    /** Unicode emoji with a Discord shortcode of `:purse:`. Represented in Unicode as 👛. */
    object Purse : UnicodeEmoji("\ud83d\udc5b")

    /** Unicode emoji with a Discord shortcode of `:handbag:`. Represented in Unicode as 👜. */
    object Handbag : UnicodeEmoji("\ud83d\udc5c")

    /** Unicode emoji with a Discord shortcode of `:briefcase:`. Represented in Unicode as 💼. */
    object Briefcase : UnicodeEmoji("\ud83d\udcbc")

    /** Unicode emoji with a Discord shortcode of `:school_satchel:`. Represented in Unicode as 🎒. */
    object SchoolSatchel : UnicodeEmoji("\ud83c\udf92")

    /** Unicode emoji with a Discord shortcode of `:luggage:`. Represented in Unicode as 🧳. */
    object Luggage : UnicodeEmoji("\ud83e\uddf3")

    /** Unicode emoji with a Discord shortcode of `:eyeglasses:`. Represented in Unicode as 👓. */
    object Eyeglasses : UnicodeEmoji("\ud83d\udc53")

    /** Unicode emoji with a Discord shortcode of `:dark_sunglasses:`. Represented in Unicode as 🕶️. */
    object DarkSunglasses : UnicodeEmoji("\ud83d\udd76\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:goggles:`. Represented in Unicode as 🥽. */
    object Goggles : UnicodeEmoji("\ud83e\udd7d")

    /** Unicode emoji with a Discord shortcode of `:diving_mask:`. Represented in Unicode as 🤿. */
    object DivingMask : UnicodeEmoji("\ud83e\udd3f")

    /** Unicode emoji with a Discord shortcode of `:closed_umbrella:`. Represented in Unicode as 🌂. */
    object ClosedUmbrella : UnicodeEmoji("\ud83c\udf02")

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

    /** Unicode emoji with Discord shortcodes of `:fox:` and `:fox_face:`. Represented in Unicode as 🦊. */
    object Fox : UnicodeEmoji("\ud83e\udd8a")

    /** Unicode emoji with a Discord shortcode of `:bear:`. Represented in Unicode as 🐻. */
    object Bear : UnicodeEmoji("\ud83d\udc3b")

    /** Unicode emoji with a Discord shortcode of `:panda_face:`. Represented in Unicode as 🐼. */
    object PandaFace : UnicodeEmoji("\ud83d\udc3c")

    /** Unicode emoji with a Discord shortcode of `:koala:`. Represented in Unicode as 🐨. */
    object Koala : UnicodeEmoji("\ud83d\udc28")

    /** Unicode emoji with a Discord shortcode of `:tiger:`. Represented in Unicode as 🐯. */
    object Tiger : UnicodeEmoji("\ud83d\udc2f")

    /** Unicode emoji with Discord shortcodes of `:lion:` and `:lion_face:`. Represented in Unicode as 🦁. */
    object Lion : UnicodeEmoji("\ud83e\udd81")

    /** Unicode emoji with a Discord shortcode of `:cow:`. Represented in Unicode as 🐮. */
    object Cow : UnicodeEmoji("\ud83d\udc2e")

    /** Unicode emoji with a Discord shortcode of `:pig:`. Represented in Unicode as 🐷. */
    object Pig : UnicodeEmoji("\ud83d\udc37")

    /** Unicode emoji with a Discord shortcode of `:pig_nose:`. Represented in Unicode as 🐽. */
    object PigNose : UnicodeEmoji("\ud83d\udc3d")

    /** Unicode emoji with a Discord shortcode of `:frog:`. Represented in Unicode as 🐸. */
    object Frog : UnicodeEmoji("\ud83d\udc38")

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

    /** Unicode emoji with a Discord shortcode of `:duck:`. Represented in Unicode as 🦆. */
    object Duck : UnicodeEmoji("\ud83e\udd86")

    /** Unicode emoji with a Discord shortcode of `:eagle:`. Represented in Unicode as 🦅. */
    object Eagle : UnicodeEmoji("\ud83e\udd85")

    /** Unicode emoji with a Discord shortcode of `:owl:`. Represented in Unicode as 🦉. */
    object Owl : UnicodeEmoji("\ud83e\udd89")

    /** Unicode emoji with a Discord shortcode of `:bat:`. Represented in Unicode as 🦇. */
    object Bat : UnicodeEmoji("\ud83e\udd87")

    /** Unicode emoji with a Discord shortcode of `:wolf:`. Represented in Unicode as 🐺. */
    object Wolf : UnicodeEmoji("\ud83d\udc3a")

    /** Unicode emoji with a Discord shortcode of `:boar:`. Represented in Unicode as 🐗. */
    object Boar : UnicodeEmoji("\ud83d\udc17")

    /** Unicode emoji with a Discord shortcode of `:horse:`. Represented in Unicode as 🐴. */
    object Horse : UnicodeEmoji("\ud83d\udc34")

    /** Unicode emoji with Discord shortcodes of `:unicorn:` and `:unicorn_face:`. Represented in Unicode as 🦄. */
    object Unicorn : UnicodeEmoji("\ud83e\udd84")

    /** Unicode emoji with a Discord shortcode of `:bee:`. Represented in Unicode as 🐝. */
    object Bee : UnicodeEmoji("\ud83d\udc1d")

    /** Unicode emoji with a Discord shortcode of `:bug:`. Represented in Unicode as 🐛. */
    object Bug : UnicodeEmoji("\ud83d\udc1b")

    /** Unicode emoji with a Discord shortcode of `:butterfly:`. Represented in Unicode as 🦋. */
    object Butterfly : UnicodeEmoji("\ud83e\udd8b")

    /** Unicode emoji with a Discord shortcode of `:snail:`. Represented in Unicode as 🐌. */
    object Snail : UnicodeEmoji("\ud83d\udc0c")

    /** Unicode emoji with a Discord shortcode of `:shell:`. Represented in Unicode as 🐚. */
    object Shell : UnicodeEmoji("\ud83d\udc1a")

    /** Unicode emoji with a Discord shortcode of `:beetle:`. Represented in Unicode as 🐞. */
    object Beetle : UnicodeEmoji("\ud83d\udc1e")

    /** Unicode emoji with a Discord shortcode of `:ant:`. Represented in Unicode as 🐜. */
    object Ant : UnicodeEmoji("\ud83d\udc1c")

    /** Unicode emoji with a Discord shortcode of `:mosquito:`. Represented in Unicode as 🦟. */
    object Mosquito : UnicodeEmoji("\ud83e\udd9f")

    /** Unicode emoji with a Discord shortcode of `:cricket:`. Represented in Unicode as 🦗. */
    object Cricket : UnicodeEmoji("\ud83e\udd97")

    /** Unicode emoji with a Discord shortcode of `:spider:`. Represented in Unicode as 🕷️. */
    object Spider : UnicodeEmoji("\ud83d\udd77\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:spider_web:`. Represented in Unicode as 🕸️. */
    object SpiderWeb : UnicodeEmoji("\ud83d\udd78\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:scorpion:`. Represented in Unicode as 🦂. */
    object Scorpion : UnicodeEmoji("\ud83e\udd82")

    /** Unicode emoji with a Discord shortcode of `:turtle:`. Represented in Unicode as 🐢. */
    object Turtle : UnicodeEmoji("\ud83d\udc22")

    /** Unicode emoji with a Discord shortcode of `:snake:`. Represented in Unicode as 🐍. */
    object Snake : UnicodeEmoji("\ud83d\udc0d")

    /** Unicode emoji with a Discord shortcode of `:lizard:`. Represented in Unicode as 🦎. */
    object Lizard : UnicodeEmoji("\ud83e\udd8e")

    /** Unicode emoji with a Discord shortcode of `:t_rex:`. Represented in Unicode as 🦖. */
    object TRex : UnicodeEmoji("\ud83e\udd96")

    /** Unicode emoji with a Discord shortcode of `:sauropod:`. Represented in Unicode as 🦕. */
    object Sauropod : UnicodeEmoji("\ud83e\udd95")

    /** Unicode emoji with a Discord shortcode of `:octopus:`. Represented in Unicode as 🐙. */
    object Octopus : UnicodeEmoji("\ud83d\udc19")

    /** Unicode emoji with a Discord shortcode of `:squid:`. Represented in Unicode as 🦑. */
    object Squid : UnicodeEmoji("\ud83e\udd91")

    /** Unicode emoji with a Discord shortcode of `:shrimp:`. Represented in Unicode as 🦐. */
    object Shrimp : UnicodeEmoji("\ud83e\udd90")

    /** Unicode emoji with a Discord shortcode of `:lobster:`. Represented in Unicode as 🦞. */
    object Lobster : UnicodeEmoji("\ud83e\udd9e")

    /** Unicode emoji with a Discord shortcode of `:oyster:`. Represented in Unicode as 🦪. */
    object Oyster : UnicodeEmoji("\ud83e\uddaa")

    /** Unicode emoji with a Discord shortcode of `:crab:`. Represented in Unicode as 🦀. */
    object Crab : UnicodeEmoji("\ud83e\udd80")

    /** Unicode emoji with a Discord shortcode of `:blowfish:`. Represented in Unicode as 🐡. */
    object Blowfish : UnicodeEmoji("\ud83d\udc21")

    /** Unicode emoji with a Discord shortcode of `:tropical_fish:`. Represented in Unicode as 🐠. */
    object TropicalFish : UnicodeEmoji("\ud83d\udc20")

    /** Unicode emoji with a Discord shortcode of `:fish:`. Represented in Unicode as 🐟. */
    object Fish : UnicodeEmoji("\ud83d\udc1f")

    /** Unicode emoji with a Discord shortcode of `:dolphin:`. Represented in Unicode as 🐬. */
    object Dolphin : UnicodeEmoji("\ud83d\udc2c")

    /** Unicode emoji with a Discord shortcode of `:whale:`. Represented in Unicode as 🐳. */
    object Whale : UnicodeEmoji("\ud83d\udc33")

    /** Unicode emoji with a Discord shortcode of `:whale2:`. Represented in Unicode as 🐋. */
    object Whale2 : UnicodeEmoji("\ud83d\udc0b")

    /** Unicode emoji with a Discord shortcode of `:shark:`. Represented in Unicode as 🦈. */
    object Shark : UnicodeEmoji("\ud83e\udd88")

    /** Unicode emoji with a Discord shortcode of `:crocodile:`. Represented in Unicode as 🐊. */
    object Crocodile : UnicodeEmoji("\ud83d\udc0a")

    /** Unicode emoji with a Discord shortcode of `:tiger2:`. Represented in Unicode as 🐅. */
    object Tiger2 : UnicodeEmoji("\ud83d\udc05")

    /** Unicode emoji with a Discord shortcode of `:leopard:`. Represented in Unicode as 🐆. */
    object Leopard : UnicodeEmoji("\ud83d\udc06")

    /** Unicode emoji with a Discord shortcode of `:zebra:`. Represented in Unicode as 🦓. */
    object Zebra : UnicodeEmoji("\ud83e\udd93")

    /** Unicode emoji with a Discord shortcode of `:gorilla:`. Represented in Unicode as 🦍. */
    object Gorilla : UnicodeEmoji("\ud83e\udd8d")

    /** Unicode emoji with a Discord shortcode of `:orangutan:`. Represented in Unicode as 🦧. */
    object Orangutan : UnicodeEmoji("\ud83e\udda7")

    /** Unicode emoji with a Discord shortcode of `:elephant:`. Represented in Unicode as 🐘. */
    object Elephant : UnicodeEmoji("\ud83d\udc18")

    /** Unicode emoji with a Discord shortcode of `:hippopotamus:`. Represented in Unicode as 🦛. */
    object Hippopotamus : UnicodeEmoji("\ud83e\udd9b")

    /** Unicode emoji with Discord shortcodes of `:rhino:` and `:rhinoceros:`. Represented in Unicode as 🦏. */
    object Rhino : UnicodeEmoji("\ud83e\udd8f")

    /** Unicode emoji with a Discord shortcode of `:dromedary_camel:`. Represented in Unicode as 🐪. */
    object DromedaryCamel : UnicodeEmoji("\ud83d\udc2a")

    /** Unicode emoji with a Discord shortcode of `:camel:`. Represented in Unicode as 🐫. */
    object Camel : UnicodeEmoji("\ud83d\udc2b")

    /** Unicode emoji with a Discord shortcode of `:giraffe:`. Represented in Unicode as 🦒. */
    object Giraffe : UnicodeEmoji("\ud83e\udd92")

    /** Unicode emoji with a Discord shortcode of `:kangaroo:`. Represented in Unicode as 🦘. */
    object Kangaroo : UnicodeEmoji("\ud83e\udd98")

    /** Unicode emoji with a Discord shortcode of `:water_buffalo:`. Represented in Unicode as 🐃. */
    object WaterBuffalo : UnicodeEmoji("\ud83d\udc03")

    /** Unicode emoji with a Discord shortcode of `:ox:`. Represented in Unicode as 🐂. */
    object OX : UnicodeEmoji("\ud83d\udc02")

    /** Unicode emoji with a Discord shortcode of `:cow2:`. Represented in Unicode as 🐄. */
    object Cow2 : UnicodeEmoji("\ud83d\udc04")

    /** Unicode emoji with a Discord shortcode of `:racehorse:`. Represented in Unicode as 🐎. */
    object Racehorse : UnicodeEmoji("\ud83d\udc0e")

    /** Unicode emoji with a Discord shortcode of `:pig2:`. Represented in Unicode as 🐖. */
    object Pig2 : UnicodeEmoji("\ud83d\udc16")

    /** Unicode emoji with a Discord shortcode of `:ram:`. Represented in Unicode as 🐏. */
    object Ram : UnicodeEmoji("\ud83d\udc0f")

    /** Unicode emoji with a Discord shortcode of `:llama:`. Represented in Unicode as 🦙. */
    object Llama : UnicodeEmoji("\ud83e\udd99")

    /** Unicode emoji with a Discord shortcode of `:sheep:`. Represented in Unicode as 🐑. */
    object Sheep : UnicodeEmoji("\ud83d\udc11")

    /** Unicode emoji with a Discord shortcode of `:goat:`. Represented in Unicode as 🐐. */
    object Goat : UnicodeEmoji("\ud83d\udc10")

    /** Unicode emoji with a Discord shortcode of `:deer:`. Represented in Unicode as 🦌. */
    object Deer : UnicodeEmoji("\ud83e\udd8c")

    /** Unicode emoji with a Discord shortcode of `:dog2:`. Represented in Unicode as 🐕. */
    object Dog2 : UnicodeEmoji("\ud83d\udc15")

    /** Unicode emoji with a Discord shortcode of `:guide_dog:`. Represented in Unicode as 🦮. */
    object GuideDog : UnicodeEmoji("\ud83e\uddae")

    /** Unicode emoji with a Discord shortcode of `:service_dog:`. Represented in Unicode as 🐕‍🦺. */
    object ServiceDog : UnicodeEmoji("\ud83d\udc15\u200d\ud83e\uddba")

    /** Unicode emoji with a Discord shortcode of `:poodle:`. Represented in Unicode as 🐩. */
    object Poodle : UnicodeEmoji("\ud83d\udc29")

    /** Unicode emoji with a Discord shortcode of `:cat2:`. Represented in Unicode as 🐈. */
    object Cat2 : UnicodeEmoji("\ud83d\udc08")

    /** Unicode emoji with a Discord shortcode of `:rooster:`. Represented in Unicode as 🐓. */
    object Rooster : UnicodeEmoji("\ud83d\udc13")

    /** Unicode emoji with a Discord shortcode of `:turkey:`. Represented in Unicode as 🦃. */
    object Turkey : UnicodeEmoji("\ud83e\udd83")

    /** Unicode emoji with a Discord shortcode of `:peacock:`. Represented in Unicode as 🦚. */
    object Peacock : UnicodeEmoji("\ud83e\udd9a")

    /** Unicode emoji with a Discord shortcode of `:parrot:`. Represented in Unicode as 🦜. */
    object Parrot : UnicodeEmoji("\ud83e\udd9c")

    /** Unicode emoji with a Discord shortcode of `:swan:`. Represented in Unicode as 🦢. */
    object Swan : UnicodeEmoji("\ud83e\udda2")

    /** Unicode emoji with a Discord shortcode of `:flamingo:`. Represented in Unicode as 🦩. */
    object Flamingo : UnicodeEmoji("\ud83e\udda9")

    /** Unicode emoji with Discord shortcodes of `:dove:` and `:dove_of_peace:`. Represented in Unicode as 🕊️. */
    object Dove : UnicodeEmoji("\ud83d\udd4a\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:rabbit2:`. Represented in Unicode as 🐇. */
    object Rabbit2 : UnicodeEmoji("\ud83d\udc07")

    /** Unicode emoji with a Discord shortcode of `:sloth:`. Represented in Unicode as 🦥. */
    object Sloth : UnicodeEmoji("\ud83e\udda5")

    /** Unicode emoji with a Discord shortcode of `:otter:`. Represented in Unicode as 🦦. */
    object Otter : UnicodeEmoji("\ud83e\udda6")

    /** Unicode emoji with a Discord shortcode of `:skunk:`. Represented in Unicode as 🦨. */
    object Skunk : UnicodeEmoji("\ud83e\udda8")

    /** Unicode emoji with a Discord shortcode of `:raccoon:`. Represented in Unicode as 🦝. */
    object Raccoon : UnicodeEmoji("\ud83e\udd9d")

    /** Unicode emoji with a Discord shortcode of `:badger:`. Represented in Unicode as 🦡. */
    object Badger : UnicodeEmoji("\ud83e\udda1")

    /** Unicode emoji with a Discord shortcode of `:mouse2:`. Represented in Unicode as 🐁. */
    object Mouse2 : UnicodeEmoji("\ud83d\udc01")

    /** Unicode emoji with a Discord shortcode of `:rat:`. Represented in Unicode as 🐀. */
    object Rat : UnicodeEmoji("\ud83d\udc00")

    /** Unicode emoji with a Discord shortcode of `:chipmunk:`. Represented in Unicode as 🐿️. */
    object Chipmunk : UnicodeEmoji("\ud83d\udc3f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:hedgehog:`. Represented in Unicode as 🦔. */
    object Hedgehog : UnicodeEmoji("\ud83e\udd94")

    /** Unicode emoji with Discord shortcodes of `:feet:` and `:paw_prints:`. Represented in Unicode as 🐾. */
    object Feet : UnicodeEmoji("\ud83d\udc3e")

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

    /** Unicode emoji with a Discord shortcode of `:shamrock:`. Represented in Unicode as ☘️. */
    object Shamrock : UnicodeEmoji("\u2618\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:mushroom:`. Represented in Unicode as 🍄. */
    object Mushroom : UnicodeEmoji("\ud83c\udf44")

    /** Unicode emoji with a Discord shortcode of `:ear_of_rice:`. Represented in Unicode as 🌾. */
    object EarOfRice : UnicodeEmoji("\ud83c\udf3e")

    /** Unicode emoji with a Discord shortcode of `:bouquet:`. Represented in Unicode as 💐. */
    object Bouquet : UnicodeEmoji("\ud83d\udc90")

    /** Unicode emoji with a Discord shortcode of `:tulip:`. Represented in Unicode as 🌷. */
    object Tulip : UnicodeEmoji("\ud83c\udf37")

    /** Unicode emoji with a Discord shortcode of `:rose:`. Represented in Unicode as 🌹. */
    object Rose : UnicodeEmoji("\ud83c\udf39")

    /** Unicode emoji with Discord shortcodes of `:wilted_rose:` and `:wilted_flower:`. Represented in Unicode as 🥀. */
    object WiltedRose : UnicodeEmoji("\ud83e\udd40")

    /** Unicode emoji with a Discord shortcode of `:hibiscus:`. Represented in Unicode as 🌺. */
    object Hibiscus : UnicodeEmoji("\ud83c\udf3a")

    /** Unicode emoji with a Discord shortcode of `:cherry_blossom:`. Represented in Unicode as 🌸. */
    object CherryBlossom : UnicodeEmoji("\ud83c\udf38")

    /** Unicode emoji with a Discord shortcode of `:blossom:`. Represented in Unicode as 🌼. */
    object Blossom : UnicodeEmoji("\ud83c\udf3c")

    /** Unicode emoji with a Discord shortcode of `:sunflower:`. Represented in Unicode as 🌻. */
    object Sunflower : UnicodeEmoji("\ud83c\udf3b")

    /** Unicode emoji with a Discord shortcode of `:sun_with_face:`. Represented in Unicode as 🌞. */
    object SunWithFace : UnicodeEmoji("\ud83c\udf1e")

    /** Unicode emoji with a Discord shortcode of `:full_moon_with_face:`. Represented in Unicode as 🌝. */
    object FullMoonWithFace : UnicodeEmoji("\ud83c\udf1d")

    /** Unicode emoji with a Discord shortcode of `:first_quarter_moon_with_face:`. Represented in Unicode as 🌛. */
    object FirstQuarterMoonWithFace : UnicodeEmoji("\ud83c\udf1b")

    /** Unicode emoji with a Discord shortcode of `:last_quarter_moon_with_face:`. Represented in Unicode as 🌜. */
    object LastQuarterMoonWithFace : UnicodeEmoji("\ud83c\udf1c")

    /** Unicode emoji with a Discord shortcode of `:new_moon_with_face:`. Represented in Unicode as 🌚. */
    object NewMoonWithFace : UnicodeEmoji("\ud83c\udf1a")

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

    /** Unicode emoji with a Discord shortcode of `:waxing_gibbous_moon:`. Represented in Unicode as 🌔. */
    object WaxingGibbousMoon : UnicodeEmoji("\ud83c\udf14")

    /** Unicode emoji with a Discord shortcode of `:crescent_moon:`. Represented in Unicode as 🌙. */
    object CrescentMoon : UnicodeEmoji("\ud83c\udf19")

    /** Unicode emoji with a Discord shortcode of `:earth_americas:`. Represented in Unicode as 🌎. */
    object EarthAmericas : UnicodeEmoji("\ud83c\udf0e")

    /** Unicode emoji with a Discord shortcode of `:earth_africa:`. Represented in Unicode as 🌍. */
    object EarthAfrica : UnicodeEmoji("\ud83c\udf0d")

    /** Unicode emoji with a Discord shortcode of `:earth_asia:`. Represented in Unicode as 🌏. */
    object EarthAsia : UnicodeEmoji("\ud83c\udf0f")

    /** Unicode emoji with a Discord shortcode of `:ringed_planet:`. Represented in Unicode as 🪐. */
    object RingedPlanet : UnicodeEmoji("\ud83e\ude90")

    /** Unicode emoji with a Discord shortcode of `:dizzy:`. Represented in Unicode as 💫. */
    object Dizzy : UnicodeEmoji("\ud83d\udcab")

    /** Unicode emoji with a Discord shortcode of `:star:`. Represented in Unicode as ⭐. */
    object Star : UnicodeEmoji("\u2b50")

    /** Unicode emoji with a Discord shortcode of `:star2:`. Represented in Unicode as 🌟. */
    object Star2 : UnicodeEmoji("\ud83c\udf1f")

    /** Unicode emoji with a Discord shortcode of `:sparkles:`. Represented in Unicode as ✨. */
    object Sparkles : UnicodeEmoji("\u2728")

    /** Unicode emoji with a Discord shortcode of `:zap:`. Represented in Unicode as ⚡. */
    object Zap : UnicodeEmoji("\u26a1")

    /** Unicode emoji with a Discord shortcode of `:comet:`. Represented in Unicode as ☄️. */
    object Comet : UnicodeEmoji("\u2604\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:boom:`. Represented in Unicode as 💥. */
    object Boom : UnicodeEmoji("\ud83d\udca5")

    /** Unicode emoji with Discord shortcodes of `:fire:` and `:flame:`. Represented in Unicode as 🔥. */
    object Fire : UnicodeEmoji("\ud83d\udd25")

    /** Unicode emoji with Discord shortcodes of `:cloud_tornado:` and `:cloud_with_tornado:`. Represented in Unicode as 🌪️. */
    object CloudTornado : UnicodeEmoji("\ud83c\udf2a\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:rainbow:`. Represented in Unicode as 🌈. */
    object Rainbow : UnicodeEmoji("\ud83c\udf08")

    /** Unicode emoji with a Discord shortcode of `:sunny:`. Represented in Unicode as ☀️. */
    object Sunny : UnicodeEmoji("\u2600\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:white_sun_small_cloud:` and `:white_sun_with_small_cloud:`. Represented in Unicode as 🌤️. */
    object WhiteSunSmallCloud : UnicodeEmoji("\ud83c\udf24\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:partly_sunny:`. Represented in Unicode as ⛅. */
    object PartlySunny : UnicodeEmoji("\u26c5")

    /** Unicode emoji with Discord shortcodes of `:white_sun_cloud:` and `:white_sun_behind_cloud:`. Represented in Unicode as 🌥️. */
    object WhiteSunCloud : UnicodeEmoji("\ud83c\udf25\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:cloud:`. Represented in Unicode as ☁️. */
    object Cloud : UnicodeEmoji("\u2601\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:white_sun_rain_cloud:` and `:white_sun_behind_cloud_with_rain:`. Represented in Unicode as 🌦️. */
    object WhiteSunRainCloud : UnicodeEmoji("\ud83c\udf26\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cloud_rain:` and `:cloud_with_rain:`. Represented in Unicode as 🌧️. */
    object CloudRain : UnicodeEmoji("\ud83c\udf27\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:thunder_cloud_rain:` and `:thunder_cloud_and_rain:`. Represented in Unicode as ⛈️. */
    object ThunderCloudRain : UnicodeEmoji("\u26c8\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cloud_lightning:` and `:cloud_with_lightning:`. Represented in Unicode as 🌩️. */
    object CloudLightning : UnicodeEmoji("\ud83c\udf29\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cloud_snow:` and `:cloud_with_snow:`. Represented in Unicode as 🌨️. */
    object CloudSnow : UnicodeEmoji("\ud83c\udf28\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:snowflake:`. Represented in Unicode as ❄️. */
    object Snowflake : UnicodeEmoji("\u2744\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:snowman2:`. Represented in Unicode as ☃️. */
    object Snowman2 : UnicodeEmoji("\u2603\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:snowman:`. Represented in Unicode as ⛄. */
    object Snowman : UnicodeEmoji("\u26c4")

    /** Unicode emoji with a Discord shortcode of `:wind_blowing_face:`. Represented in Unicode as 🌬️. */
    object WindBlowingFace : UnicodeEmoji("\ud83c\udf2c\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:dash:`. Represented in Unicode as 💨. */
    object Dash : UnicodeEmoji("\ud83d\udca8")

    /** Unicode emoji with a Discord shortcode of `:droplet:`. Represented in Unicode as 💧. */
    object Droplet : UnicodeEmoji("\ud83d\udca7")

    /** Unicode emoji with a Discord shortcode of `:sweat_drops:`. Represented in Unicode as 💦. */
    object SweatDrops : UnicodeEmoji("\ud83d\udca6")

    /** Unicode emoji with a Discord shortcode of `:umbrella:`. Represented in Unicode as ☔. */
    object Umbrella : UnicodeEmoji("\u2614")

    /** Unicode emoji with a Discord shortcode of `:umbrella2:`. Represented in Unicode as ☂️. */
    object Umbrella2 : UnicodeEmoji("\u2602\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ocean:`. Represented in Unicode as 🌊. */
    object Ocean : UnicodeEmoji("\ud83c\udf0a")

    /** Unicode emoji with a Discord shortcode of `:fog:`. Represented in Unicode as 🌫️. */
    object Fog : UnicodeEmoji("\ud83c\udf2b\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:mango:`. Represented in Unicode as 🥭. */
    object Mango : UnicodeEmoji("\ud83e\udd6d")

    /** Unicode emoji with a Discord shortcode of `:pineapple:`. Represented in Unicode as 🍍. */
    object Pineapple : UnicodeEmoji("\ud83c\udf4d")

    /** Unicode emoji with a Discord shortcode of `:coconut:`. Represented in Unicode as 🥥. */
    object Coconut : UnicodeEmoji("\ud83e\udd65")

    /** Unicode emoji with Discord shortcodes of `:kiwi:` and `:kiwifruit:`. Represented in Unicode as 🥝. */
    object Kiwi : UnicodeEmoji("\ud83e\udd5d")

    /** Unicode emoji with a Discord shortcode of `:tomato:`. Represented in Unicode as 🍅. */
    object Tomato : UnicodeEmoji("\ud83c\udf45")

    /** Unicode emoji with a Discord shortcode of `:eggplant:`. Represented in Unicode as 🍆. */
    object Eggplant : UnicodeEmoji("\ud83c\udf46")

    /** Unicode emoji with a Discord shortcode of `:avocado:`. Represented in Unicode as 🥑. */
    object Avocado : UnicodeEmoji("\ud83e\udd51")

    /** Unicode emoji with a Discord shortcode of `:broccoli:`. Represented in Unicode as 🥦. */
    object Broccoli : UnicodeEmoji("\ud83e\udd66")

    /** Unicode emoji with a Discord shortcode of `:leafy_green:`. Represented in Unicode as 🥬. */
    object LeafyGreen : UnicodeEmoji("\ud83e\udd6c")

    /** Unicode emoji with a Discord shortcode of `:cucumber:`. Represented in Unicode as 🥒. */
    object Cucumber : UnicodeEmoji("\ud83e\udd52")

    /** Unicode emoji with a Discord shortcode of `:hot_pepper:`. Represented in Unicode as 🌶️. */
    object HotPepper : UnicodeEmoji("\ud83c\udf36\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:corn:`. Represented in Unicode as 🌽. */
    object Corn : UnicodeEmoji("\ud83c\udf3d")

    /** Unicode emoji with a Discord shortcode of `:carrot:`. Represented in Unicode as 🥕. */
    object Carrot : UnicodeEmoji("\ud83e\udd55")

    /** Unicode emoji with a Discord shortcode of `:onion:`. Represented in Unicode as 🧅. */
    object Onion : UnicodeEmoji("\ud83e\uddc5")

    /** Unicode emoji with a Discord shortcode of `:garlic:`. Represented in Unicode as 🧄. */
    object Garlic : UnicodeEmoji("\ud83e\uddc4")

    /** Unicode emoji with a Discord shortcode of `:potato:`. Represented in Unicode as 🥔. */
    object Potato : UnicodeEmoji("\ud83e\udd54")

    /** Unicode emoji with a Discord shortcode of `:sweet_potato:`. Represented in Unicode as 🍠. */
    object SweetPotato : UnicodeEmoji("\ud83c\udf60")

    /** Unicode emoji with a Discord shortcode of `:croissant:`. Represented in Unicode as 🥐. */
    object Croissant : UnicodeEmoji("\ud83e\udd50")

    /** Unicode emoji with a Discord shortcode of `:bagel:`. Represented in Unicode as 🥯. */
    object Bagel : UnicodeEmoji("\ud83e\udd6f")

    /** Unicode emoji with a Discord shortcode of `:bread:`. Represented in Unicode as 🍞. */
    object Bread : UnicodeEmoji("\ud83c\udf5e")

    /** Unicode emoji with Discord shortcodes of `:french_bread:` and `:baguette_bread:`. Represented in Unicode as 🥖. */
    object FrenchBread : UnicodeEmoji("\ud83e\udd56")

    /** Unicode emoji with a Discord shortcode of `:pretzel:`. Represented in Unicode as 🥨. */
    object Pretzel : UnicodeEmoji("\ud83e\udd68")

    /** Unicode emoji with Discord shortcodes of `:cheese:` and `:cheese_wedge:`. Represented in Unicode as 🧀. */
    object Cheese : UnicodeEmoji("\ud83e\uddc0")

    /** Unicode emoji with a Discord shortcode of `:egg:`. Represented in Unicode as 🥚. */
    object Egg : UnicodeEmoji("\ud83e\udd5a")

    /** Unicode emoji with a Discord shortcode of `:cooking:`. Represented in Unicode as 🍳. */
    object Cooking : UnicodeEmoji("\ud83c\udf73")

    /** Unicode emoji with a Discord shortcode of `:pancakes:`. Represented in Unicode as 🥞. */
    object Pancakes : UnicodeEmoji("\ud83e\udd5e")

    /** Unicode emoji with a Discord shortcode of `:waffle:`. Represented in Unicode as 🧇. */
    object Waffle : UnicodeEmoji("\ud83e\uddc7")

    /** Unicode emoji with a Discord shortcode of `:bacon:`. Represented in Unicode as 🥓. */
    object Bacon : UnicodeEmoji("\ud83e\udd53")

    /** Unicode emoji with a Discord shortcode of `:cut_of_meat:`. Represented in Unicode as 🥩. */
    object CutOfMeat : UnicodeEmoji("\ud83e\udd69")

    /** Unicode emoji with a Discord shortcode of `:poultry_leg:`. Represented in Unicode as 🍗. */
    object PoultryLeg : UnicodeEmoji("\ud83c\udf57")

    /** Unicode emoji with a Discord shortcode of `:meat_on_bone:`. Represented in Unicode as 🍖. */
    object MeatOnBone : UnicodeEmoji("\ud83c\udf56")

    /** Unicode emoji with Discord shortcodes of `:hotdog:` and `:hot_dog:`. Represented in Unicode as 🌭. */
    object Hotdog : UnicodeEmoji("\ud83c\udf2d")

    /** Unicode emoji with a Discord shortcode of `:hamburger:`. Represented in Unicode as 🍔. */
    object Hamburger : UnicodeEmoji("\ud83c\udf54")

    /** Unicode emoji with a Discord shortcode of `:fries:`. Represented in Unicode as 🍟. */
    object Fries : UnicodeEmoji("\ud83c\udf5f")

    /** Unicode emoji with a Discord shortcode of `:pizza:`. Represented in Unicode as 🍕. */
    object Pizza : UnicodeEmoji("\ud83c\udf55")

    /** Unicode emoji with a Discord shortcode of `:sandwich:`. Represented in Unicode as 🥪. */
    object Sandwich : UnicodeEmoji("\ud83e\udd6a")

    /** Unicode emoji with a Discord shortcode of `:falafel:`. Represented in Unicode as 🧆. */
    object Falafel : UnicodeEmoji("\ud83e\uddc6")

    /** Unicode emoji with Discord shortcodes of `:stuffed_pita:` and `:stuffed_flatbread:`. Represented in Unicode as 🥙. */
    object StuffedPita : UnicodeEmoji("\ud83e\udd59")

    /** Unicode emoji with a Discord shortcode of `:taco:`. Represented in Unicode as 🌮. */
    object Taco : UnicodeEmoji("\ud83c\udf2e")

    /** Unicode emoji with a Discord shortcode of `:burrito:`. Represented in Unicode as 🌯. */
    object Burrito : UnicodeEmoji("\ud83c\udf2f")

    /** Unicode emoji with Discord shortcodes of `:salad:` and `:green_salad:`. Represented in Unicode as 🥗. */
    object Salad : UnicodeEmoji("\ud83e\udd57")

    /** Unicode emoji with Discord shortcodes of `:paella:` and `:shallow_pan_of_food:`. Represented in Unicode as 🥘. */
    object Paella : UnicodeEmoji("\ud83e\udd58")

    /** Unicode emoji with a Discord shortcode of `:canned_food:`. Represented in Unicode as 🥫. */
    object CannedFood : UnicodeEmoji("\ud83e\udd6b")

    /** Unicode emoji with a Discord shortcode of `:spaghetti:`. Represented in Unicode as 🍝. */
    object Spaghetti : UnicodeEmoji("\ud83c\udf5d")

    /** Unicode emoji with a Discord shortcode of `:ramen:`. Represented in Unicode as 🍜. */
    object Ramen : UnicodeEmoji("\ud83c\udf5c")

    /** Unicode emoji with a Discord shortcode of `:stew:`. Represented in Unicode as 🍲. */
    object Stew : UnicodeEmoji("\ud83c\udf72")

    /** Unicode emoji with a Discord shortcode of `:curry:`. Represented in Unicode as 🍛. */
    object Curry : UnicodeEmoji("\ud83c\udf5b")

    /** Unicode emoji with a Discord shortcode of `:sushi:`. Represented in Unicode as 🍣. */
    object Sushi : UnicodeEmoji("\ud83c\udf63")

    /** Unicode emoji with a Discord shortcode of `:bento:`. Represented in Unicode as 🍱. */
    object Bento : UnicodeEmoji("\ud83c\udf71")

    /** Unicode emoji with a Discord shortcode of `:dumpling:`. Represented in Unicode as 🥟. */
    object Dumpling : UnicodeEmoji("\ud83e\udd5f")

    /** Unicode emoji with a Discord shortcode of `:fried_shrimp:`. Represented in Unicode as 🍤. */
    object FriedShrimp : UnicodeEmoji("\ud83c\udf64")

    /** Unicode emoji with a Discord shortcode of `:rice_ball:`. Represented in Unicode as 🍙. */
    object RiceBall : UnicodeEmoji("\ud83c\udf59")

    /** Unicode emoji with a Discord shortcode of `:rice:`. Represented in Unicode as 🍚. */
    object Rice : UnicodeEmoji("\ud83c\udf5a")

    /** Unicode emoji with a Discord shortcode of `:rice_cracker:`. Represented in Unicode as 🍘. */
    object RiceCracker : UnicodeEmoji("\ud83c\udf58")

    /** Unicode emoji with a Discord shortcode of `:fish_cake:`. Represented in Unicode as 🍥. */
    object FishCake : UnicodeEmoji("\ud83c\udf65")

    /** Unicode emoji with a Discord shortcode of `:fortune_cookie:`. Represented in Unicode as 🥠. */
    object FortuneCookie : UnicodeEmoji("\ud83e\udd60")

    /** Unicode emoji with a Discord shortcode of `:moon_cake:`. Represented in Unicode as 🥮. */
    object MoonCake : UnicodeEmoji("\ud83e\udd6e")

    /** Unicode emoji with a Discord shortcode of `:oden:`. Represented in Unicode as 🍢. */
    object Oden : UnicodeEmoji("\ud83c\udf62")

    /** Unicode emoji with a Discord shortcode of `:dango:`. Represented in Unicode as 🍡. */
    object Dango : UnicodeEmoji("\ud83c\udf61")

    /** Unicode emoji with a Discord shortcode of `:shaved_ice:`. Represented in Unicode as 🍧. */
    object ShavedIce : UnicodeEmoji("\ud83c\udf67")

    /** Unicode emoji with a Discord shortcode of `:ice_cream:`. Represented in Unicode as 🍨. */
    object IceCream : UnicodeEmoji("\ud83c\udf68")

    /** Unicode emoji with a Discord shortcode of `:icecream:`. Represented in Unicode as 🍦. */
    object Icecream : UnicodeEmoji("\ud83c\udf66")

    /** Unicode emoji with a Discord shortcode of `:pie:`. Represented in Unicode as 🥧. */
    object Pie : UnicodeEmoji("\ud83e\udd67")

    /** Unicode emoji with a Discord shortcode of `:cupcake:`. Represented in Unicode as 🧁. */
    object Cupcake : UnicodeEmoji("\ud83e\uddc1")

    /** Unicode emoji with a Discord shortcode of `:cake:`. Represented in Unicode as 🍰. */
    object Cake : UnicodeEmoji("\ud83c\udf70")

    /** Unicode emoji with a Discord shortcode of `:birthday:`. Represented in Unicode as 🎂. */
    object Birthday : UnicodeEmoji("\ud83c\udf82")

    /** Unicode emoji with Discord shortcodes of `:flan:`, `:custard:`, and `:pudding:`. Represented in Unicode as 🍮. */
    object Flan : UnicodeEmoji("\ud83c\udf6e")

    /** Unicode emoji with a Discord shortcode of `:lollipop:`. Represented in Unicode as 🍭. */
    object Lollipop : UnicodeEmoji("\ud83c\udf6d")

    /** Unicode emoji with a Discord shortcode of `:candy:`. Represented in Unicode as 🍬. */
    object Candy : UnicodeEmoji("\ud83c\udf6c")

    /** Unicode emoji with a Discord shortcode of `:chocolate_bar:`. Represented in Unicode as 🍫. */
    object ChocolateBar : UnicodeEmoji("\ud83c\udf6b")

    /** Unicode emoji with a Discord shortcode of `:popcorn:`. Represented in Unicode as 🍿. */
    object Popcorn : UnicodeEmoji("\ud83c\udf7f")

    /** Unicode emoji with a Discord shortcode of `:doughnut:`. Represented in Unicode as 🍩. */
    object Doughnut : UnicodeEmoji("\ud83c\udf69")

    /** Unicode emoji with a Discord shortcode of `:cookie:`. Represented in Unicode as 🍪. */
    object Cookie : UnicodeEmoji("\ud83c\udf6a")

    /** Unicode emoji with a Discord shortcode of `:chestnut:`. Represented in Unicode as 🌰. */
    object Chestnut : UnicodeEmoji("\ud83c\udf30")

    /** Unicode emoji with Discord shortcodes of `:peanuts:` and `:shelled_peanut:`. Represented in Unicode as 🥜. */
    object Peanuts : UnicodeEmoji("\ud83e\udd5c")

    /** Unicode emoji with a Discord shortcode of `:honey_pot:`. Represented in Unicode as 🍯. */
    object HoneyPot : UnicodeEmoji("\ud83c\udf6f")

    /** Unicode emoji with a Discord shortcode of `:butter:`. Represented in Unicode as 🧈. */
    object Butter : UnicodeEmoji("\ud83e\uddc8")

    /** Unicode emoji with Discord shortcodes of `:milk:` and `:glass_of_milk:`. Represented in Unicode as 🥛. */
    object Milk : UnicodeEmoji("\ud83e\udd5b")

    /** Unicode emoji with a Discord shortcode of `:baby_bottle:`. Represented in Unicode as 🍼. */
    object BabyBottle : UnicodeEmoji("\ud83c\udf7c")

    /** Unicode emoji with a Discord shortcode of `:coffee:`. Represented in Unicode as ☕. */
    object Coffee : UnicodeEmoji("\u2615")

    /** Unicode emoji with a Discord shortcode of `:tea:`. Represented in Unicode as 🍵. */
    object Tea : UnicodeEmoji("\ud83c\udf75")

    /** Unicode emoji with a Discord shortcode of `:mate:`. Represented in Unicode as 🧉. */
    object Mate : UnicodeEmoji("\ud83e\uddc9")

    /** Unicode emoji with a Discord shortcode of `:cup_with_straw:`. Represented in Unicode as 🥤. */
    object CupWithStraw : UnicodeEmoji("\ud83e\udd64")

    /** Unicode emoji with a Discord shortcode of `:beverage_box:`. Represented in Unicode as 🧃. */
    object BeverageBox : UnicodeEmoji("\ud83e\uddc3")

    /** Unicode emoji with a Discord shortcode of `:ice_cube:`. Represented in Unicode as 🧊. */
    object IceCube : UnicodeEmoji("\ud83e\uddca")

    /** Unicode emoji with a Discord shortcode of `:sake:`. Represented in Unicode as 🍶. */
    object Sake : UnicodeEmoji("\ud83c\udf76")

    /** Unicode emoji with a Discord shortcode of `:beer:`. Represented in Unicode as 🍺. */
    object Beer : UnicodeEmoji("\ud83c\udf7a")

    /** Unicode emoji with a Discord shortcode of `:beers:`. Represented in Unicode as 🍻. */
    object Beers : UnicodeEmoji("\ud83c\udf7b")

    /** Unicode emoji with Discord shortcodes of `:clinking_glass:` and `:champagne_glass:`. Represented in Unicode as 🥂. */
    object ClinkingGlass : UnicodeEmoji("\ud83e\udd42")

    /** Unicode emoji with a Discord shortcode of `:wine_glass:`. Represented in Unicode as 🍷. */
    object WineGlass : UnicodeEmoji("\ud83c\udf77")

    /** Unicode emoji with Discord shortcodes of `:whisky:` and `:tumbler_glass:`. Represented in Unicode as 🥃. */
    object Whisky : UnicodeEmoji("\ud83e\udd43")

    /** Unicode emoji with a Discord shortcode of `:cocktail:`. Represented in Unicode as 🍸. */
    object Cocktail : UnicodeEmoji("\ud83c\udf78")

    /** Unicode emoji with a Discord shortcode of `:tropical_drink:`. Represented in Unicode as 🍹. */
    object TropicalDrink : UnicodeEmoji("\ud83c\udf79")

    /** Unicode emoji with Discord shortcodes of `:champagne:` and `:bottle_with_popping_cork:`. Represented in Unicode as 🍾. */
    object Champagne : UnicodeEmoji("\ud83c\udf7e")

    /** Unicode emoji with a Discord shortcode of `:spoon:`. Represented in Unicode as 🥄. */
    object Spoon : UnicodeEmoji("\ud83e\udd44")

    /** Unicode emoji with a Discord shortcode of `:fork_and_knife:`. Represented in Unicode as 🍴. */
    object ForkAndKnife : UnicodeEmoji("\ud83c\udf74")

    /** Unicode emoji with Discord shortcodes of `:fork_knife_plate:` and `:fork_and_knife_with_plate:`. Represented in Unicode as 🍽️. */
    object ForkKnifePlate : UnicodeEmoji("\ud83c\udf7d\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:bowl_with_spoon:`. Represented in Unicode as 🥣. */
    object BowlWithSpoon : UnicodeEmoji("\ud83e\udd63")

    /** Unicode emoji with a Discord shortcode of `:takeout_box:`. Represented in Unicode as 🥡. */
    object TakeoutBox : UnicodeEmoji("\ud83e\udd61")

    /** Unicode emoji with a Discord shortcode of `:chopsticks:`. Represented in Unicode as 🥢. */
    object Chopsticks : UnicodeEmoji("\ud83e\udd62")

    /** Unicode emoji with a Discord shortcode of `:salt:`. Represented in Unicode as 🧂. */
    object Salt : UnicodeEmoji("\ud83e\uddc2")

    /** Unicode emoji with a Discord shortcode of `:soccer:`. Represented in Unicode as ⚽. */
    object Soccer : UnicodeEmoji("\u26bd")

    /** Unicode emoji with a Discord shortcode of `:basketball:`. Represented in Unicode as 🏀. */
    object Basketball : UnicodeEmoji("\ud83c\udfc0")

    /** Unicode emoji with a Discord shortcode of `:football:`. Represented in Unicode as 🏈. */
    object Football : UnicodeEmoji("\ud83c\udfc8")

    /** Unicode emoji with a Discord shortcode of `:baseball:`. Represented in Unicode as ⚾. */
    object Baseball : UnicodeEmoji("\u26be")

    /** Unicode emoji with a Discord shortcode of `:softball:`. Represented in Unicode as 🥎. */
    object Softball : UnicodeEmoji("\ud83e\udd4e")

    /** Unicode emoji with a Discord shortcode of `:tennis:`. Represented in Unicode as 🎾. */
    object Tennis : UnicodeEmoji("\ud83c\udfbe")

    /** Unicode emoji with a Discord shortcode of `:volleyball:`. Represented in Unicode as 🏐. */
    object Volleyball : UnicodeEmoji("\ud83c\udfd0")

    /** Unicode emoji with a Discord shortcode of `:rugby_football:`. Represented in Unicode as 🏉. */
    object RugbyFootball : UnicodeEmoji("\ud83c\udfc9")

    /** Unicode emoji with a Discord shortcode of `:flying_disc:`. Represented in Unicode as 🥏. */
    object FlyingDisc : UnicodeEmoji("\ud83e\udd4f")

    /** Unicode emoji with a Discord shortcode of `:8ball:`. Represented in Unicode as 🎱. */
    object EightBall : UnicodeEmoji("\ud83c\udfb1")

    /** Unicode emoji with Discord shortcodes of `:ping_pong:` and `:table_tennis:`. Represented in Unicode as 🏓. */
    object PingPong : UnicodeEmoji("\ud83c\udfd3")

    /** Unicode emoji with a Discord shortcode of `:badminton:`. Represented in Unicode as 🏸. */
    object Badminton : UnicodeEmoji("\ud83c\udff8")

    /** Unicode emoji with a Discord shortcode of `:hockey:`. Represented in Unicode as 🏒. */
    object Hockey : UnicodeEmoji("\ud83c\udfd2")

    /** Unicode emoji with a Discord shortcode of `:field_hockey:`. Represented in Unicode as 🏑. */
    object FieldHockey : UnicodeEmoji("\ud83c\udfd1")

    /** Unicode emoji with a Discord shortcode of `:lacrosse:`. Represented in Unicode as 🥍. */
    object Lacrosse : UnicodeEmoji("\ud83e\udd4d")

    /** Unicode emoji with Discord shortcodes of `:cricket_game:` and `:cricket_bat_ball:`. Represented in Unicode as 🏏. */
    object CricketGame : UnicodeEmoji("\ud83c\udfcf")

    /** Unicode emoji with Discord shortcodes of `:goal:` and `:goal_net:`. Represented in Unicode as 🥅. */
    object Goal : UnicodeEmoji("\ud83e\udd45")

    /** Unicode emoji with a Discord shortcode of `:golf:`. Represented in Unicode as ⛳. */
    object Golf : UnicodeEmoji("\u26f3")

    /** Unicode emoji with Discord shortcodes of `:archery:` and `:bow_and_arrow:`. Represented in Unicode as 🏹. */
    object Archery : UnicodeEmoji("\ud83c\udff9")

    /** Unicode emoji with a Discord shortcode of `:fishing_pole_and_fish:`. Represented in Unicode as 🎣. */
    object FishingPoleAndFish : UnicodeEmoji("\ud83c\udfa3")

    /** Unicode emoji with Discord shortcodes of `:boxing_glove:` and `:boxing_gloves:`. Represented in Unicode as 🥊. */
    object BoxingGlove : UnicodeEmoji("\ud83e\udd4a")

    /** Unicode emoji with Discord shortcodes of `:karate_uniform:` and `:martial_arts_uniform:`. Represented in Unicode as 🥋. */
    object KarateUniform : UnicodeEmoji("\ud83e\udd4b")

    /** Unicode emoji with a Discord shortcode of `:running_shirt_with_sash:`. Represented in Unicode as 🎽. */
    object RunningShirtWithSash : UnicodeEmoji("\ud83c\udfbd")

    /** Unicode emoji with a Discord shortcode of `:skateboard:`. Represented in Unicode as 🛹. */
    object Skateboard : UnicodeEmoji("\ud83d\udef9")

    /** Unicode emoji with a Discord shortcode of `:sled:`. Represented in Unicode as 🛷. */
    object Sled : UnicodeEmoji("\ud83d\udef7")

    /** Unicode emoji with a Discord shortcode of `:parachute:`. Represented in Unicode as 🪂. */
    object Parachute : UnicodeEmoji("\ud83e\ude82")

    /** Unicode emoji with a Discord shortcode of `:ice_skate:`. Represented in Unicode as ⛸️. */
    object IceSkate : UnicodeEmoji("\u26f8\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:curling_stone:`. Represented in Unicode as 🥌. */
    object CurlingStone : UnicodeEmoji("\ud83e\udd4c")

    /** Unicode emoji with a Discord shortcode of `:ski:`. Represented in Unicode as 🎿. */
    object Ski : UnicodeEmoji("\ud83c\udfbf")

    /** Unicode emoji with a Discord shortcode of `:skier:`. Represented in Unicode as ⛷️. */
    object Skier : UnicodeEmoji("\u26f7\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:snowboarder:`, and the given skin [tone]. Represented in Unicode as 🏂. */
    class Snowboarder(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc2", tone)

    /** Unicode emoji with Discord shortcodes of `:lifter:`, `:weight_lifter:`, and `:person_lifting_weights:`, and the given skin [tone]. Represented in Unicode as 🏋️. */
    class Lifter(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcb\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_lifting_weights:`, and the given skin [tone]. Represented in Unicode as 🏋️‍♀️. */
    class WomanLiftingWeights(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcb\ufe0f\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_lifting_weights:`, and the given skin [tone]. Represented in Unicode as 🏋️‍♂️. */
    class ManLiftingWeights(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcb\ufe0f\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:wrestlers:`, `:wrestling:`, and `:people_wrestling:`. Represented in Unicode as 🤼. */
    object Wrestlers : UnicodeEmoji("\ud83e\udd3c")

    /** Unicode emoji with a Discord shortcode of `:women_wrestling:`. Represented in Unicode as 🤼‍♀️. */
    object WomenWrestling : UnicodeEmoji("\ud83e\udd3c\u200d\u2640\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:men_wrestling:`. Represented in Unicode as 🤼‍♂️. */
    object MenWrestling : UnicodeEmoji("\ud83e\udd3c\u200d\u2642\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cartwheel:` and `:person_doing_cartwheel:`, and the given skin [tone]. Represented in Unicode as 🤸. */
    class Cartwheel(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd38", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_cartwheeling:`, and the given skin [tone]. Represented in Unicode as 🤸‍♀️. */
    class WomanCartwheeling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd38\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_cartwheeling:`, and the given skin [tone]. Represented in Unicode as 🤸‍♂️. */
    class ManCartwheeling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd38\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:person_with_ball:`, `:basketball_player:`, and `:person_bouncing_ball:`, and the given skin [tone]. Represented in Unicode as ⛹️. */
    class PersonWithBall(tone: SkinTone? = null) : UnicodeEmoji("\u26f9\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_bouncing_ball:`, and the given skin [tone]. Represented in Unicode as ⛹️‍♀️. */
    class WomanBouncingBall(tone: SkinTone? = null) : UnicodeEmoji("\u26f9\ufe0f\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_bouncing_ball:`, and the given skin [tone]. Represented in Unicode as ⛹️‍♂️. */
    class ManBouncingBall(tone: SkinTone? = null) : UnicodeEmoji("\u26f9\ufe0f\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:fencer:`, `:fencing:`, and `:person_fencing:`. Represented in Unicode as 🤺. */
    object Fencer : UnicodeEmoji("\ud83e\udd3a")

    /** Unicode emoji with Discord shortcodes of `:handball:` and `:person_playing_handball:`, and the given skin [tone]. Represented in Unicode as 🤾. */
    class Handball(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3e", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_playing_handball:`, and the given skin [tone]. Represented in Unicode as 🤾‍♀️. */
    class WomanPlayingHandball(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3e\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_playing_handball:`, and the given skin [tone]. Represented in Unicode as 🤾‍♂️. */
    class ManPlayingHandball(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3e\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:golfer:` and `:person_golfing:`, and the given skin [tone]. Represented in Unicode as 🏌️. */
    class Golfer(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcc\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_golfing:`, and the given skin [tone]. Represented in Unicode as 🏌️‍♀️. */
    class WomanGolfing(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcc\ufe0f\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_golfing:`, and the given skin [tone]. Represented in Unicode as 🏌️‍♂️. */
    class ManGolfing(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfcc\ufe0f\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:horse_racing:`, and the given skin [tone]. Represented in Unicode as 🏇. */
    class HorseRacing(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc7", tone)

    /** Unicode emoji with a Discord shortcode of `:person_in_lotus_position:`, and the given skin [tone]. Represented in Unicode as 🧘. */
    class PersonInLotusPosition(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd8", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_in_lotus_position:`, and the given skin [tone]. Represented in Unicode as 🧘‍♀️. */
    class WomanInLotusPosition(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd8\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_in_lotus_position:`, and the given skin [tone]. Represented in Unicode as 🧘‍♂️. */
    class ManInLotusPosition(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd8\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:surfer:` and `:person_surfing:`, and the given skin [tone]. Represented in Unicode as 🏄. */
    class Surfer(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc4", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_surfing:`, and the given skin [tone]. Represented in Unicode as 🏄‍♀️. */
    class WomanSurfing(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc4\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_surfing:`, and the given skin [tone]. Represented in Unicode as 🏄‍♂️. */
    class ManSurfing(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfc4\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:swimmer:` and `:person_swimming:`, and the given skin [tone]. Represented in Unicode as 🏊. */
    class Swimmer(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfca", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_swimming:`, and the given skin [tone]. Represented in Unicode as 🏊‍♀️. */
    class WomanSwimming(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfca\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_swimming:`, and the given skin [tone]. Represented in Unicode as 🏊‍♂️. */
    class ManSwimming(tone: SkinTone? = null) : UnicodeEmoji("\ud83c\udfca\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:water_polo:` and `:person_playing_water_polo:`, and the given skin [tone]. Represented in Unicode as 🤽. */
    class WaterPolo(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3d", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_playing_water_polo:`, and the given skin [tone]. Represented in Unicode as 🤽‍♀️. */
    class WomanPlayingWaterPolo(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3d\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_playing_water_polo:`, and the given skin [tone]. Represented in Unicode as 🤽‍♂️. */
    class ManPlayingWaterPolo(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd3d\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:rowboat:` and `:person_rowing_boat:`, and the given skin [tone]. Represented in Unicode as 🚣. */
    class Rowboat(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udea3", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_rowing_boat:`, and the given skin [tone]. Represented in Unicode as 🚣‍♀️. */
    class WomanRowingBoat(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udea3\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_rowing_boat:`, and the given skin [tone]. Represented in Unicode as 🚣‍♂️. */
    class ManRowingBoat(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udea3\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:person_climbing:`, and the given skin [tone]. Represented in Unicode as 🧗. */
    class PersonClimbing(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd7", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_climbing:`, and the given skin [tone]. Represented in Unicode as 🧗‍♀️. */
    class WomanClimbing(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd7\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_climbing:`, and the given skin [tone]. Represented in Unicode as 🧗‍♂️. */
    class ManClimbing(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\uddd7\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:mountain_bicyclist:` and `:person_mountain_biking:`, and the given skin [tone]. Represented in Unicode as 🚵. */
    class MountainBicyclist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb5", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_mountain_biking:`, and the given skin [tone]. Represented in Unicode as 🚵‍♀️. */
    class WomanMountainBiking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb5\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_mountain_biking:`, and the given skin [tone]. Represented in Unicode as 🚵‍♂️. */
    class ManMountainBiking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb5\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with Discord shortcodes of `:bicyclist:` and `:person_biking:`, and the given skin [tone]. Represented in Unicode as 🚴. */
    class Bicyclist(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb4", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_biking:`, and the given skin [tone]. Represented in Unicode as 🚴‍♀️. */
    class WomanBiking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb4\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_biking:`, and the given skin [tone]. Represented in Unicode as 🚴‍♂️. */
    class ManBiking(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udeb4\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:trophy:`. Represented in Unicode as 🏆. */
    object Trophy : UnicodeEmoji("\ud83c\udfc6")

    /** Unicode emoji with Discord shortcodes of `:first_place:` and `:first_place_medal:`. Represented in Unicode as 🥇. */
    object FirstPlace : UnicodeEmoji("\ud83e\udd47")

    /** Unicode emoji with Discord shortcodes of `:second_place:` and `:second_place_medal:`. Represented in Unicode as 🥈. */
    object SecondPlace : UnicodeEmoji("\ud83e\udd48")

    /** Unicode emoji with Discord shortcodes of `:third_place:` and `:third_place_medal:`. Represented in Unicode as 🥉. */
    object ThirdPlace : UnicodeEmoji("\ud83e\udd49")

    /** Unicode emoji with Discord shortcodes of `:medal:` and `:sports_medal:`. Represented in Unicode as 🏅. */
    object Medal : UnicodeEmoji("\ud83c\udfc5")

    /** Unicode emoji with a Discord shortcode of `:military_medal:`. Represented in Unicode as 🎖️. */
    object MilitaryMedal : UnicodeEmoji("\ud83c\udf96\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:rosette:`. Represented in Unicode as 🏵️. */
    object Rosette : UnicodeEmoji("\ud83c\udff5\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:reminder_ribbon:`. Represented in Unicode as 🎗️. */
    object ReminderRibbon : UnicodeEmoji("\ud83c\udf97\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ticket:`. Represented in Unicode as 🎫. */
    object Ticket : UnicodeEmoji("\ud83c\udfab")

    /** Unicode emoji with Discord shortcodes of `:tickets:` and `:admission_tickets:`. Represented in Unicode as 🎟️. */
    object Tickets : UnicodeEmoji("\ud83c\udf9f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:circus_tent:`. Represented in Unicode as 🎪. */
    object CircusTent : UnicodeEmoji("\ud83c\udfaa")

    /** Unicode emoji with Discord shortcodes of `:juggler:`, `:juggling:`, and `:person_juggling:`, and the given skin [tone]. Represented in Unicode as 🤹. */
    class Juggler(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd39", tone)

    /** Unicode emoji with a Discord shortcode of `:woman_juggling:`, and the given skin [tone]. Represented in Unicode as 🤹‍♀️. */
    class WomanJuggling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd39\u200d\u2640\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:man_juggling:`, and the given skin [tone]. Represented in Unicode as 🤹‍♂️. */
    class ManJuggling(tone: SkinTone? = null) : UnicodeEmoji("\ud83e\udd39\u200d\u2642\ufe0f", tone)

    /** Unicode emoji with a Discord shortcode of `:performing_arts:`. Represented in Unicode as 🎭. */
    object PerformingArts : UnicodeEmoji("\ud83c\udfad")

    /** Unicode emoji with a Discord shortcode of `:art:`. Represented in Unicode as 🎨. */
    object Art : UnicodeEmoji("\ud83c\udfa8")

    /** Unicode emoji with a Discord shortcode of `:clapper:`. Represented in Unicode as 🎬. */
    object Clapper : UnicodeEmoji("\ud83c\udfac")

    /** Unicode emoji with a Discord shortcode of `:microphone:`. Represented in Unicode as 🎤. */
    object Microphone : UnicodeEmoji("\ud83c\udfa4")

    /** Unicode emoji with a Discord shortcode of `:headphones:`. Represented in Unicode as 🎧. */
    object Headphones : UnicodeEmoji("\ud83c\udfa7")

    /** Unicode emoji with a Discord shortcode of `:musical_score:`. Represented in Unicode as 🎼. */
    object MusicalScore : UnicodeEmoji("\ud83c\udfbc")

    /** Unicode emoji with a Discord shortcode of `:musical_keyboard:`. Represented in Unicode as 🎹. */
    object MusicalKeyboard : UnicodeEmoji("\ud83c\udfb9")

    /** Unicode emoji with Discord shortcodes of `:drum:` and `:drum_with_drumsticks:`. Represented in Unicode as 🥁. */
    object Drum : UnicodeEmoji("\ud83e\udd41")

    /** Unicode emoji with a Discord shortcode of `:saxophone:`. Represented in Unicode as 🎷. */
    object Saxophone : UnicodeEmoji("\ud83c\udfb7")

    /** Unicode emoji with a Discord shortcode of `:trumpet:`. Represented in Unicode as 🎺. */
    object Trumpet : UnicodeEmoji("\ud83c\udfba")

    /** Unicode emoji with a Discord shortcode of `:banjo:`. Represented in Unicode as 🪕. */
    object Banjo : UnicodeEmoji("\ud83e\ude95")

    /** Unicode emoji with a Discord shortcode of `:guitar:`. Represented in Unicode as 🎸. */
    object Guitar : UnicodeEmoji("\ud83c\udfb8")

    /** Unicode emoji with a Discord shortcode of `:violin:`. Represented in Unicode as 🎻. */
    object Violin : UnicodeEmoji("\ud83c\udfbb")

    /** Unicode emoji with a Discord shortcode of `:game_die:`. Represented in Unicode as 🎲. */
    object GameDie : UnicodeEmoji("\ud83c\udfb2")

    /** Unicode emoji with a Discord shortcode of `:chess_pawn:`. Represented in Unicode as ♟️. */
    object ChessPawn : UnicodeEmoji("\u265f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:dart:`. Represented in Unicode as 🎯. */
    object Dart : UnicodeEmoji("\ud83c\udfaf")

    /** Unicode emoji with a Discord shortcode of `:kite:`. Represented in Unicode as 🪁. */
    object Kite : UnicodeEmoji("\ud83e\ude81")

    /** Unicode emoji with a Discord shortcode of `:yo_yo:`. Represented in Unicode as 🪀. */
    object YoYo : UnicodeEmoji("\ud83e\ude80")

    /** Unicode emoji with a Discord shortcode of `:bowling:`. Represented in Unicode as 🎳. */
    object Bowling : UnicodeEmoji("\ud83c\udfb3")

    /** Unicode emoji with a Discord shortcode of `:video_game:`. Represented in Unicode as 🎮. */
    object VideoGame : UnicodeEmoji("\ud83c\udfae")

    /** Unicode emoji with a Discord shortcode of `:slot_machine:`. Represented in Unicode as 🎰. */
    object SlotMachine : UnicodeEmoji("\ud83c\udfb0")

    /** Unicode emoji with a Discord shortcode of `:jigsaw:`. Represented in Unicode as 🧩. */
    object Jigsaw : UnicodeEmoji("\ud83e\udde9")

    /** Unicode emoji with a Discord shortcode of `:red_car:`. Represented in Unicode as 🚗. */
    object RedCar : UnicodeEmoji("\ud83d\ude97")

    /** Unicode emoji with a Discord shortcode of `:taxi:`. Represented in Unicode as 🚕. */
    object Taxi : UnicodeEmoji("\ud83d\ude95")

    /** Unicode emoji with a Discord shortcode of `:blue_car:`. Represented in Unicode as 🚙. */
    object BlueCar : UnicodeEmoji("\ud83d\ude99")

    /** Unicode emoji with a Discord shortcode of `:bus:`. Represented in Unicode as 🚌. */
    object Bus : UnicodeEmoji("\ud83d\ude8c")

    /** Unicode emoji with a Discord shortcode of `:trolleybus:`. Represented in Unicode as 🚎. */
    object Trolleybus : UnicodeEmoji("\ud83d\ude8e")

    /** Unicode emoji with Discord shortcodes of `:race_car:` and `:racing_car:`. Represented in Unicode as 🏎️. */
    object RaceCar : UnicodeEmoji("\ud83c\udfce\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:auto_rickshaw:`. Represented in Unicode as 🛺. */
    object AutoRickshaw : UnicodeEmoji("\ud83d\udefa")

    /** Unicode emoji with Discord shortcodes of `:motorbike:` and `:motor_scooter:`. Represented in Unicode as 🛵. */
    object Motorbike : UnicodeEmoji("\ud83d\udef5")

    /** Unicode emoji with Discord shortcodes of `:motorcycle:` and `:racing_motorcycle:`. Represented in Unicode as 🏍️. */
    object Motorcycle : UnicodeEmoji("\ud83c\udfcd\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:scooter:`. Represented in Unicode as 🛴. */
    object Scooter : UnicodeEmoji("\ud83d\udef4")

    /** Unicode emoji with a Discord shortcode of `:bike:`. Represented in Unicode as 🚲. */
    object Bike : UnicodeEmoji("\ud83d\udeb2")

    /** Unicode emoji with a Discord shortcode of `:motorized_wheelchair:`. Represented in Unicode as 🦼. */
    object MotorizedWheelchair : UnicodeEmoji("\ud83e\uddbc")

    /** Unicode emoji with a Discord shortcode of `:manual_wheelchair:`. Represented in Unicode as 🦽. */
    object ManualWheelchair : UnicodeEmoji("\ud83e\uddbd")

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

    /** Unicode emoji with a Discord shortcode of `:mountain_railway:`. Represented in Unicode as 🚞. */
    object MountainRailway : UnicodeEmoji("\ud83d\ude9e")

    /** Unicode emoji with a Discord shortcode of `:monorail:`. Represented in Unicode as 🚝. */
    object Monorail : UnicodeEmoji("\ud83d\ude9d")

    /** Unicode emoji with a Discord shortcode of `:bullettrain_side:`. Represented in Unicode as 🚄. */
    object BullettrainSide : UnicodeEmoji("\ud83d\ude84")

    /** Unicode emoji with a Discord shortcode of `:bullettrain_front:`. Represented in Unicode as 🚅. */
    object BullettrainFront : UnicodeEmoji("\ud83d\ude85")

    /** Unicode emoji with a Discord shortcode of `:light_rail:`. Represented in Unicode as 🚈. */
    object LightRail : UnicodeEmoji("\ud83d\ude88")

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

    /** Unicode emoji with a Discord shortcode of `:airplane:`. Represented in Unicode as ✈️. */
    object Airplane : UnicodeEmoji("\u2708\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:airplane_departure:`. Represented in Unicode as 🛫. */
    object AirplaneDeparture : UnicodeEmoji("\ud83d\udeeb")

    /** Unicode emoji with a Discord shortcode of `:airplane_arriving:`. Represented in Unicode as 🛬. */
    object AirplaneArriving : UnicodeEmoji("\ud83d\udeec")

    /** Unicode emoji with Discord shortcodes of `:airplane_small:` and `:small_airplane:`. Represented in Unicode as 🛩️. */
    object AirplaneSmall : UnicodeEmoji("\ud83d\udee9\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:seat:`. Represented in Unicode as 💺. */
    object Seat : UnicodeEmoji("\ud83d\udcba")

    /** Unicode emoji with a Discord shortcode of `:satellite_orbital:`. Represented in Unicode as 🛰️. */
    object SatelliteOrbital : UnicodeEmoji("\ud83d\udef0\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:rocket:`. Represented in Unicode as 🚀. */
    object Rocket : UnicodeEmoji("\ud83d\ude80")

    /** Unicode emoji with a Discord shortcode of `:flying_saucer:`. Represented in Unicode as 🛸. */
    object FlyingSaucer : UnicodeEmoji("\ud83d\udef8")

    /** Unicode emoji with a Discord shortcode of `:helicopter:`. Represented in Unicode as 🚁. */
    object Helicopter : UnicodeEmoji("\ud83d\ude81")

    /** Unicode emoji with Discord shortcodes of `:canoe:` and `:kayak:`. Represented in Unicode as 🛶. */
    object Canoe : UnicodeEmoji("\ud83d\udef6")

    /** Unicode emoji with a Discord shortcode of `:sailboat:`. Represented in Unicode as ⛵. */
    object Sailboat : UnicodeEmoji("\u26f5")

    /** Unicode emoji with a Discord shortcode of `:speedboat:`. Represented in Unicode as 🚤. */
    object Speedboat : UnicodeEmoji("\ud83d\udea4")

    /** Unicode emoji with a Discord shortcode of `:motorboat:`. Represented in Unicode as 🛥️. */
    object Motorboat : UnicodeEmoji("\ud83d\udee5\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cruise_ship:` and `:passenger_ship:`. Represented in Unicode as 🛳️. */
    object CruiseShip : UnicodeEmoji("\ud83d\udef3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ferry:`. Represented in Unicode as ⛴️. */
    object Ferry : UnicodeEmoji("\u26f4\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ship:`. Represented in Unicode as 🚢. */
    object Ship : UnicodeEmoji("\ud83d\udea2")

    /** Unicode emoji with a Discord shortcode of `:anchor:`. Represented in Unicode as ⚓. */
    object Anchor : UnicodeEmoji("\u2693")

    /** Unicode emoji with a Discord shortcode of `:fuelpump:`. Represented in Unicode as ⛽. */
    object Fuelpump : UnicodeEmoji("\u26fd")

    /** Unicode emoji with a Discord shortcode of `:construction:`. Represented in Unicode as 🚧. */
    object Construction : UnicodeEmoji("\ud83d\udea7")

    /** Unicode emoji with a Discord shortcode of `:vertical_traffic_light:`. Represented in Unicode as 🚦. */
    object VerticalTrafficLight : UnicodeEmoji("\ud83d\udea6")

    /** Unicode emoji with a Discord shortcode of `:traffic_light:`. Represented in Unicode as 🚥. */
    object TrafficLight : UnicodeEmoji("\ud83d\udea5")

    /** Unicode emoji with a Discord shortcode of `:busstop:`. Represented in Unicode as 🚏. */
    object Busstop : UnicodeEmoji("\ud83d\ude8f")

    /** Unicode emoji with Discord shortcodes of `:map:` and `:world_map:`. Represented in Unicode as 🗺️. */
    object Map : UnicodeEmoji("\ud83d\uddfa\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:moyai:`. Represented in Unicode as 🗿. */
    object Moyai : UnicodeEmoji("\ud83d\uddff")

    /** Unicode emoji with a Discord shortcode of `:statue_of_liberty:`. Represented in Unicode as 🗽. */
    object StatueOfLiberty : UnicodeEmoji("\ud83d\uddfd")

    /** Unicode emoji with a Discord shortcode of `:tokyo_tower:`. Represented in Unicode as 🗼. */
    object TokyoTower : UnicodeEmoji("\ud83d\uddfc")

    /** Unicode emoji with a Discord shortcode of `:european_castle:`. Represented in Unicode as 🏰. */
    object EuropeanCastle : UnicodeEmoji("\ud83c\udff0")

    /** Unicode emoji with a Discord shortcode of `:japanese_castle:`. Represented in Unicode as 🏯. */
    object JapaneseCastle : UnicodeEmoji("\ud83c\udfef")

    /** Unicode emoji with a Discord shortcode of `:stadium:`. Represented in Unicode as 🏟️. */
    object Stadium : UnicodeEmoji("\ud83c\udfdf\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ferris_wheel:`. Represented in Unicode as 🎡. */
    object FerrisWheel : UnicodeEmoji("\ud83c\udfa1")

    /** Unicode emoji with a Discord shortcode of `:roller_coaster:`. Represented in Unicode as 🎢. */
    object RollerCoaster : UnicodeEmoji("\ud83c\udfa2")

    /** Unicode emoji with a Discord shortcode of `:carousel_horse:`. Represented in Unicode as 🎠. */
    object CarouselHorse : UnicodeEmoji("\ud83c\udfa0")

    /** Unicode emoji with a Discord shortcode of `:fountain:`. Represented in Unicode as ⛲. */
    object Fountain : UnicodeEmoji("\u26f2")

    /** Unicode emoji with Discord shortcodes of `:beach_umbrella:` and `:umbrella_on_ground:`. Represented in Unicode as ⛱️. */
    object BeachUmbrella : UnicodeEmoji("\u26f1\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:beach:` and `:beach_with_umbrella:`. Represented in Unicode as 🏖️. */
    object Beach : UnicodeEmoji("\ud83c\udfd6\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:island:` and `:desert_island:`. Represented in Unicode as 🏝️. */
    object Island : UnicodeEmoji("\ud83c\udfdd\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:desert:`. Represented in Unicode as 🏜️. */
    object Desert : UnicodeEmoji("\ud83c\udfdc\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:volcano:`. Represented in Unicode as 🌋. */
    object Volcano : UnicodeEmoji("\ud83c\udf0b")

    /** Unicode emoji with a Discord shortcode of `:mountain:`. Represented in Unicode as ⛰️. */
    object Mountain : UnicodeEmoji("\u26f0\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:mountain_snow:` and `:snow_capped_mountain:`. Represented in Unicode as 🏔️. */
    object MountainSnow : UnicodeEmoji("\ud83c\udfd4\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:mount_fuji:`. Represented in Unicode as 🗻. */
    object MountFuji : UnicodeEmoji("\ud83d\uddfb")

    /** Unicode emoji with a Discord shortcode of `:camping:`. Represented in Unicode as 🏕️. */
    object Camping : UnicodeEmoji("\ud83c\udfd5\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:tent:`. Represented in Unicode as ⛺. */
    object Tent : UnicodeEmoji("\u26fa")

    /** Unicode emoji with a Discord shortcode of `:house:`. Represented in Unicode as 🏠. */
    object House : UnicodeEmoji("\ud83c\udfe0")

    /** Unicode emoji with a Discord shortcode of `:house_with_garden:`. Represented in Unicode as 🏡. */
    object HouseWithGarden : UnicodeEmoji("\ud83c\udfe1")

    /** Unicode emoji with Discord shortcodes of `:homes:` and `:house_buildings:`. Represented in Unicode as 🏘️. */
    object Homes : UnicodeEmoji("\ud83c\udfd8\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:house_abandoned:` and `:derelict_house_building:`. Represented in Unicode as 🏚️. */
    object HouseAbandoned : UnicodeEmoji("\ud83c\udfda\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:construction_site:` and `:building_construction:`. Represented in Unicode as 🏗️. */
    object ConstructionSite : UnicodeEmoji("\ud83c\udfd7\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:factory:`. Represented in Unicode as 🏭. */
    object Factory : UnicodeEmoji("\ud83c\udfed")

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

    /** Unicode emoji with a Discord shortcode of `:classical_building:`. Represented in Unicode as 🏛️. */
    object ClassicalBuilding : UnicodeEmoji("\ud83c\udfdb\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:church:`. Represented in Unicode as ⛪. */
    object Church : UnicodeEmoji("\u26ea")

    /** Unicode emoji with a Discord shortcode of `:mosque:`. Represented in Unicode as 🕌. */
    object Mosque : UnicodeEmoji("\ud83d\udd4c")

    /** Unicode emoji with a Discord shortcode of `:hindu_temple:`. Represented in Unicode as 🛕. */
    object HinduTemple : UnicodeEmoji("\ud83d\uded5")

    /** Unicode emoji with a Discord shortcode of `:synagogue:`. Represented in Unicode as 🕍. */
    object Synagogue : UnicodeEmoji("\ud83d\udd4d")

    /** Unicode emoji with a Discord shortcode of `:kaaba:`. Represented in Unicode as 🕋. */
    object Kaaba : UnicodeEmoji("\ud83d\udd4b")

    /** Unicode emoji with a Discord shortcode of `:shinto_shrine:`. Represented in Unicode as ⛩️. */
    object ShintoShrine : UnicodeEmoji("\u26e9\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:railway_track:` and `:railroad_track:`. Represented in Unicode as 🛤️. */
    object RailwayTrack : UnicodeEmoji("\ud83d\udee4\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:motorway:`. Represented in Unicode as 🛣️. */
    object Motorway : UnicodeEmoji("\ud83d\udee3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:japan:`. Represented in Unicode as 🗾. */
    object Japan : UnicodeEmoji("\ud83d\uddfe")

    /** Unicode emoji with a Discord shortcode of `:rice_scene:`. Represented in Unicode as 🎑. */
    object RiceScene : UnicodeEmoji("\ud83c\udf91")

    /** Unicode emoji with Discord shortcodes of `:park:` and `:national_park:`. Represented in Unicode as 🏞️. */
    object Park : UnicodeEmoji("\ud83c\udfde\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:sunrise:`. Represented in Unicode as 🌅. */
    object Sunrise : UnicodeEmoji("\ud83c\udf05")

    /** Unicode emoji with a Discord shortcode of `:sunrise_over_mountains:`. Represented in Unicode as 🌄. */
    object SunriseOverMountains : UnicodeEmoji("\ud83c\udf04")

    /** Unicode emoji with a Discord shortcode of `:stars:`. Represented in Unicode as 🌠. */
    object Stars : UnicodeEmoji("\ud83c\udf20")

    /** Unicode emoji with a Discord shortcode of `:sparkler:`. Represented in Unicode as 🎇. */
    object Sparkler : UnicodeEmoji("\ud83c\udf87")

    /** Unicode emoji with a Discord shortcode of `:fireworks:`. Represented in Unicode as 🎆. */
    object Fireworks : UnicodeEmoji("\ud83c\udf86")

    /** Unicode emoji with Discord shortcodes of `:city_sunset:` and `:city_sunrise:`. Represented in Unicode as 🌇. */
    object CitySunset : UnicodeEmoji("\ud83c\udf07")

    /** Unicode emoji with a Discord shortcode of `:city_dusk:`. Represented in Unicode as 🌆. */
    object CityDusk : UnicodeEmoji("\ud83c\udf06")

    /** Unicode emoji with a Discord shortcode of `:cityscape:`. Represented in Unicode as 🏙️. */
    object Cityscape : UnicodeEmoji("\ud83c\udfd9\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:night_with_stars:`. Represented in Unicode as 🌃. */
    object NightWithStars : UnicodeEmoji("\ud83c\udf03")

    /** Unicode emoji with a Discord shortcode of `:milky_way:`. Represented in Unicode as 🌌. */
    object MilkyWay : UnicodeEmoji("\ud83c\udf0c")

    /** Unicode emoji with a Discord shortcode of `:bridge_at_night:`. Represented in Unicode as 🌉. */
    object BridgeAtNight : UnicodeEmoji("\ud83c\udf09")

    /** Unicode emoji with a Discord shortcode of `:foggy:`. Represented in Unicode as 🌁. */
    object Foggy : UnicodeEmoji("\ud83c\udf01")

    /** Unicode emoji with a Discord shortcode of `:watch:`. Represented in Unicode as ⌚. */
    object Watch : UnicodeEmoji("\u231a")

    /** Unicode emoji with a Discord shortcode of `:iphone:`. Represented in Unicode as 📱. */
    object Iphone : UnicodeEmoji("\ud83d\udcf1")

    /** Unicode emoji with a Discord shortcode of `:calling:`. Represented in Unicode as 📲. */
    object Calling : UnicodeEmoji("\ud83d\udcf2")

    /** Unicode emoji with a Discord shortcode of `:computer:`. Represented in Unicode as 💻. */
    object Computer : UnicodeEmoji("\ud83d\udcbb")

    /** Unicode emoji with a Discord shortcode of `:keyboard:`. Represented in Unicode as ⌨️. */
    object Keyboard : UnicodeEmoji("\u2328\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:desktop:` and `:desktop_computer:`. Represented in Unicode as 🖥️. */
    object Desktop : UnicodeEmoji("\ud83d\udda5\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:printer:`. Represented in Unicode as 🖨️. */
    object Printer : UnicodeEmoji("\ud83d\udda8\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:mouse_three_button:` and `:three_button_mouse:`. Represented in Unicode as 🖱️. */
    object MouseThreeButton : UnicodeEmoji("\ud83d\uddb1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:trackball:`. Represented in Unicode as 🖲️. */
    object Trackball : UnicodeEmoji("\ud83d\uddb2\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:joystick:`. Represented in Unicode as 🕹️. */
    object Joystick : UnicodeEmoji("\ud83d\udd79\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:compression:`. Represented in Unicode as 🗜️. */
    object Compression : UnicodeEmoji("\ud83d\udddc\ufe0f")

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

    /** Unicode emoji with Discord shortcodes of `:projector:` and `:film_projector:`. Represented in Unicode as 📽️. */
    object Projector : UnicodeEmoji("\ud83d\udcfd\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:film_frames:`. Represented in Unicode as 🎞️. */
    object FilmFrames : UnicodeEmoji("\ud83c\udf9e\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:telephone_receiver:`. Represented in Unicode as 📞. */
    object TelephoneReceiver : UnicodeEmoji("\ud83d\udcde")

    /** Unicode emoji with a Discord shortcode of `:telephone:`. Represented in Unicode as ☎️. */
    object Telephone : UnicodeEmoji("\u260e\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:pager:`. Represented in Unicode as 📟. */
    object Pager : UnicodeEmoji("\ud83d\udcdf")

    /** Unicode emoji with a Discord shortcode of `:fax:`. Represented in Unicode as 📠. */
    object Fax : UnicodeEmoji("\ud83d\udce0")

    /** Unicode emoji with a Discord shortcode of `:tv:`. Represented in Unicode as 📺. */
    object TV : UnicodeEmoji("\ud83d\udcfa")

    /** Unicode emoji with a Discord shortcode of `:radio:`. Represented in Unicode as 📻. */
    object Radio : UnicodeEmoji("\ud83d\udcfb")

    /** Unicode emoji with Discord shortcodes of `:microphone2:` and `:studio_microphone:`. Represented in Unicode as 🎙️. */
    object Microphone2 : UnicodeEmoji("\ud83c\udf99\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:level_slider:`. Represented in Unicode as 🎚️. */
    object LevelSlider : UnicodeEmoji("\ud83c\udf9a\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:control_knobs:`. Represented in Unicode as 🎛️. */
    object ControlKnobs : UnicodeEmoji("\ud83c\udf9b\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:compass:`. Represented in Unicode as 🧭. */
    object Compass : UnicodeEmoji("\ud83e\udded")

    /** Unicode emoji with a Discord shortcode of `:stopwatch:`. Represented in Unicode as ⏱️. */
    object Stopwatch : UnicodeEmoji("\u23f1\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:timer:` and `:timer_clock:`. Represented in Unicode as ⏲️. */
    object Timer : UnicodeEmoji("\u23f2\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:alarm_clock:`. Represented in Unicode as ⏰. */
    object AlarmClock : UnicodeEmoji("\u23f0")

    /** Unicode emoji with Discord shortcodes of `:clock:` and `:mantlepiece_clock:`. Represented in Unicode as 🕰️. */
    object Clock : UnicodeEmoji("\ud83d\udd70\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:hourglass:`. Represented in Unicode as ⌛. */
    object Hourglass : UnicodeEmoji("\u231b")

    /** Unicode emoji with a Discord shortcode of `:hourglass_flowing_sand:`. Represented in Unicode as ⏳. */
    object HourglassFlowingSand : UnicodeEmoji("\u23f3")

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

    /** Unicode emoji with a Discord shortcode of `:candle:`. Represented in Unicode as 🕯️. */
    object Candle : UnicodeEmoji("\ud83d\udd6f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:fire_extinguisher:`. Represented in Unicode as 🧯. */
    object FireExtinguisher : UnicodeEmoji("\ud83e\uddef")

    /** Unicode emoji with Discord shortcodes of `:oil:` and `:oil_drum:`. Represented in Unicode as 🛢️. */
    object Oil : UnicodeEmoji("\ud83d\udee2\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:scales:`. Represented in Unicode as ⚖️. */
    object Scales : UnicodeEmoji("\u2696\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:toolbox:`. Represented in Unicode as 🧰. */
    object Toolbox : UnicodeEmoji("\ud83e\uddf0")

    /** Unicode emoji with a Discord shortcode of `:wrench:`. Represented in Unicode as 🔧. */
    object Wrench : UnicodeEmoji("\ud83d\udd27")

    /** Unicode emoji with a Discord shortcode of `:hammer:`. Represented in Unicode as 🔨. */
    object Hammer : UnicodeEmoji("\ud83d\udd28")

    /** Unicode emoji with Discord shortcodes of `:hammer_pick:` and `:hammer_and_pick:`. Represented in Unicode as ⚒️. */
    object HammerPick : UnicodeEmoji("\u2692\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:tools:` and `:hammer_and_wrench:`. Represented in Unicode as 🛠️. */
    object Tools : UnicodeEmoji("\ud83d\udee0\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:pick:`. Represented in Unicode as ⛏️. */
    object Pick : UnicodeEmoji("\u26cf\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:nut_and_bolt:`. Represented in Unicode as 🔩. */
    object NutAndBolt : UnicodeEmoji("\ud83d\udd29")

    /** Unicode emoji with a Discord shortcode of `:gear:`. Represented in Unicode as ⚙️. */
    object Gear : UnicodeEmoji("\u2699\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:bricks:`. Represented in Unicode as 🧱. */
    object Bricks : UnicodeEmoji("\ud83e\uddf1")

    /** Unicode emoji with a Discord shortcode of `:chains:`. Represented in Unicode as ⛓️. */
    object Chains : UnicodeEmoji("\u26d3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:magnet:`. Represented in Unicode as 🧲. */
    object Magnet : UnicodeEmoji("\ud83e\uddf2")

    /** Unicode emoji with a Discord shortcode of `:gun:`. Represented in Unicode as 🔫. */
    object Gun : UnicodeEmoji("\ud83d\udd2b")

    /** Unicode emoji with a Discord shortcode of `:bomb:`. Represented in Unicode as 💣. */
    object Bomb : UnicodeEmoji("\ud83d\udca3")

    /** Unicode emoji with a Discord shortcode of `:firecracker:`. Represented in Unicode as 🧨. */
    object Firecracker : UnicodeEmoji("\ud83e\udde8")

    /** Unicode emoji with a Discord shortcode of `:axe:`. Represented in Unicode as 🪓. */
    object Axe : UnicodeEmoji("\ud83e\ude93")

    /** Unicode emoji with a Discord shortcode of `:razor:`. Represented in Unicode as 🪒. */
    object Razor : UnicodeEmoji("\ud83e\ude92")

    /** Unicode emoji with a Discord shortcode of `:knife:`. Represented in Unicode as 🔪. */
    object Knife : UnicodeEmoji("\ud83d\udd2a")

    /** Unicode emoji with Discord shortcodes of `:dagger:` and `:dagger_knife:`. Represented in Unicode as 🗡️. */
    object Dagger : UnicodeEmoji("\ud83d\udde1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:crossed_swords:`. Represented in Unicode as ⚔️. */
    object CrossedSwords : UnicodeEmoji("\u2694\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:shield:`. Represented in Unicode as 🛡️. */
    object Shield : UnicodeEmoji("\ud83d\udee1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:smoking:`. Represented in Unicode as 🚬. */
    object Smoking : UnicodeEmoji("\ud83d\udeac")

    /** Unicode emoji with a Discord shortcode of `:coffin:`. Represented in Unicode as ⚰️. */
    object Coffin : UnicodeEmoji("\u26b0\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:urn:` and `:funeral_urn:`. Represented in Unicode as ⚱️. */
    object Urn : UnicodeEmoji("\u26b1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:amphora:`. Represented in Unicode as 🏺. */
    object Amphora : UnicodeEmoji("\ud83c\udffa")

    /** Unicode emoji with a Discord shortcode of `:diya_lamp:`. Represented in Unicode as 🪔. */
    object DiyaLamp : UnicodeEmoji("\ud83e\ude94")

    /** Unicode emoji with a Discord shortcode of `:crystal_ball:`. Represented in Unicode as 🔮. */
    object CrystalBall : UnicodeEmoji("\ud83d\udd2e")

    /** Unicode emoji with a Discord shortcode of `:prayer_beads:`. Represented in Unicode as 📿. */
    object PrayerBeads : UnicodeEmoji("\ud83d\udcff")

    /** Unicode emoji with a Discord shortcode of `:nazar_amulet:`. Represented in Unicode as 🧿. */
    object NazarAmulet : UnicodeEmoji("\ud83e\uddff")

    /** Unicode emoji with a Discord shortcode of `:barber:`. Represented in Unicode as 💈. */
    object Barber : UnicodeEmoji("\ud83d\udc88")

    /** Unicode emoji with a Discord shortcode of `:alembic:`. Represented in Unicode as ⚗️. */
    object Alembic : UnicodeEmoji("\u2697\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:telescope:`. Represented in Unicode as 🔭. */
    object Telescope : UnicodeEmoji("\ud83d\udd2d")

    /** Unicode emoji with a Discord shortcode of `:microscope:`. Represented in Unicode as 🔬. */
    object Microscope : UnicodeEmoji("\ud83d\udd2c")

    /** Unicode emoji with a Discord shortcode of `:hole:`. Represented in Unicode as 🕳️. */
    object Hole : UnicodeEmoji("\ud83d\udd73\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:probing_cane:`. Represented in Unicode as 🦯. */
    object ProbingCane : UnicodeEmoji("\ud83e\uddaf")

    /** Unicode emoji with a Discord shortcode of `:stethoscope:`. Represented in Unicode as 🩺. */
    object Stethoscope : UnicodeEmoji("\ud83e\ude7a")

    /** Unicode emoji with a Discord shortcode of `:adhesive_bandage:`. Represented in Unicode as 🩹. */
    object AdhesiveBandage : UnicodeEmoji("\ud83e\ude79")

    /** Unicode emoji with a Discord shortcode of `:pill:`. Represented in Unicode as 💊. */
    object Pill : UnicodeEmoji("\ud83d\udc8a")

    /** Unicode emoji with a Discord shortcode of `:syringe:`. Represented in Unicode as 💉. */
    object Syringe : UnicodeEmoji("\ud83d\udc89")

    /** Unicode emoji with a Discord shortcode of `:drop_of_blood:`. Represented in Unicode as 🩸. */
    object DropOfBlood : UnicodeEmoji("\ud83e\ude78")

    /** Unicode emoji with a Discord shortcode of `:dna:`. Represented in Unicode as 🧬. */
    object Dna : UnicodeEmoji("\ud83e\uddec")

    /** Unicode emoji with a Discord shortcode of `:microbe:`. Represented in Unicode as 🦠. */
    object Microbe : UnicodeEmoji("\ud83e\udda0")

    /** Unicode emoji with a Discord shortcode of `:petri_dish:`. Represented in Unicode as 🧫. */
    object PetriDish : UnicodeEmoji("\ud83e\uddeb")

    /** Unicode emoji with a Discord shortcode of `:test_tube:`. Represented in Unicode as 🧪. */
    object TestTube : UnicodeEmoji("\ud83e\uddea")

    /** Unicode emoji with a Discord shortcode of `:thermometer:`. Represented in Unicode as 🌡️. */
    object Thermometer : UnicodeEmoji("\ud83c\udf21\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:chair:`. Represented in Unicode as 🪑. */
    object Chair : UnicodeEmoji("\ud83e\ude91")

    /** Unicode emoji with a Discord shortcode of `:broom:`. Represented in Unicode as 🧹. */
    object Broom : UnicodeEmoji("\ud83e\uddf9")

    /** Unicode emoji with a Discord shortcode of `:basket:`. Represented in Unicode as 🧺. */
    object Basket : UnicodeEmoji("\ud83e\uddfa")

    /** Unicode emoji with a Discord shortcode of `:roll_of_paper:`. Represented in Unicode as 🧻. */
    object RollOfPaper : UnicodeEmoji("\ud83e\uddfb")

    /** Unicode emoji with a Discord shortcode of `:toilet:`. Represented in Unicode as 🚽. */
    object Toilet : UnicodeEmoji("\ud83d\udebd")

    /** Unicode emoji with a Discord shortcode of `:potable_water:`. Represented in Unicode as 🚰. */
    object PotableWater : UnicodeEmoji("\ud83d\udeb0")

    /** Unicode emoji with a Discord shortcode of `:shower:`. Represented in Unicode as 🚿. */
    object Shower : UnicodeEmoji("\ud83d\udebf")

    /** Unicode emoji with a Discord shortcode of `:bathtub:`. Represented in Unicode as 🛁. */
    object Bathtub : UnicodeEmoji("\ud83d\udec1")

    /** Unicode emoji with a Discord shortcode of `:bath:`, and the given skin [tone]. Represented in Unicode as 🛀. */
    class Bath(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udec0", tone)

    /** Unicode emoji with a Discord shortcode of `:soap:`. Represented in Unicode as 🧼. */
    object Soap : UnicodeEmoji("\ud83e\uddfc")

    /** Unicode emoji with a Discord shortcode of `:sponge:`. Represented in Unicode as 🧽. */
    object Sponge : UnicodeEmoji("\ud83e\uddfd")

    /** Unicode emoji with a Discord shortcode of `:squeeze_bottle:`. Represented in Unicode as 🧴. */
    object SqueezeBottle : UnicodeEmoji("\ud83e\uddf4")

    /** Unicode emoji with Discord shortcodes of `:bellhop:` and `:bellhop_bell:`. Represented in Unicode as 🛎️. */
    object Bellhop : UnicodeEmoji("\ud83d\udece\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:key:`. Represented in Unicode as 🔑. */
    object Key : UnicodeEmoji("\ud83d\udd11")

    /** Unicode emoji with Discord shortcodes of `:key2:` and `:old_key:`. Represented in Unicode as 🗝️. */
    object Key2 : UnicodeEmoji("\ud83d\udddd\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:door:`. Represented in Unicode as 🚪. */
    object Door : UnicodeEmoji("\ud83d\udeaa")

    /** Unicode emoji with Discord shortcodes of `:couch:` and `:couch_and_lamp:`. Represented in Unicode as 🛋️. */
    object Couch : UnicodeEmoji("\ud83d\udecb\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:bed:`. Represented in Unicode as 🛏️. */
    object Bed : UnicodeEmoji("\ud83d\udecf\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:sleeping_accommodation:`, and the given skin [tone]. Represented in Unicode as 🛌. */
    class SleepingAccommodation(tone: SkinTone? = null) : UnicodeEmoji("\ud83d\udecc", tone)

    /** Unicode emoji with a Discord shortcode of `:teddy_bear:`. Represented in Unicode as 🧸. */
    object TeddyBear : UnicodeEmoji("\ud83e\uddf8")

    /** Unicode emoji with Discord shortcodes of `:frame_photo:` and `:frame_with_picture:`. Represented in Unicode as 🖼️. */
    object FramePhoto : UnicodeEmoji("\ud83d\uddbc\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:shopping_bags:`. Represented in Unicode as 🛍️. */
    object ShoppingBags : UnicodeEmoji("\ud83d\udecd\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:shopping_cart:` and `:shopping_trolley:`. Represented in Unicode as 🛒. */
    object ShoppingCart : UnicodeEmoji("\ud83d\uded2")

    /** Unicode emoji with a Discord shortcode of `:gift:`. Represented in Unicode as 🎁. */
    object Gift : UnicodeEmoji("\ud83c\udf81")

    /** Unicode emoji with a Discord shortcode of `:balloon:`. Represented in Unicode as 🎈. */
    object Balloon : UnicodeEmoji("\ud83c\udf88")

    /** Unicode emoji with a Discord shortcode of `:flags:`. Represented in Unicode as 🎏. */
    object FlagS : UnicodeEmoji("\ud83c\udf8f")

    /** Unicode emoji with a Discord shortcode of `:ribbon:`. Represented in Unicode as 🎀. */
    object Ribbon : UnicodeEmoji("\ud83c\udf80")

    /** Unicode emoji with a Discord shortcode of `:confetti_ball:`. Represented in Unicode as 🎊. */
    object ConfettiBall : UnicodeEmoji("\ud83c\udf8a")

    /** Unicode emoji with a Discord shortcode of `:tada:`. Represented in Unicode as 🎉. */
    object Tada : UnicodeEmoji("\ud83c\udf89")

    /** Unicode emoji with a Discord shortcode of `:dolls:`. Represented in Unicode as 🎎. */
    object Dolls : UnicodeEmoji("\ud83c\udf8e")

    /** Unicode emoji with a Discord shortcode of `:izakaya_lantern:`. Represented in Unicode as 🏮. */
    object IzakayaLantern : UnicodeEmoji("\ud83c\udfee")

    /** Unicode emoji with a Discord shortcode of `:wind_chime:`. Represented in Unicode as 🎐. */
    object WindChime : UnicodeEmoji("\ud83c\udf90")

    /** Unicode emoji with a Discord shortcode of `:red_envelope:`. Represented in Unicode as 🧧. */
    object RedEnvelope : UnicodeEmoji("\ud83e\udde7")

    /** Unicode emoji with a Discord shortcode of `:envelope:`. Represented in Unicode as ✉️. */
    object Envelope : UnicodeEmoji("\u2709\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:envelope_with_arrow:`. Represented in Unicode as 📩. */
    object EnvelopeWithArrow : UnicodeEmoji("\ud83d\udce9")

    /** Unicode emoji with a Discord shortcode of `:incoming_envelope:`. Represented in Unicode as 📨. */
    object IncomingEnvelope : UnicodeEmoji("\ud83d\udce8")

    /** Unicode emoji with Discord shortcodes of `:email:` and `:e_mail:`. Represented in Unicode as 📧. */
    object Email : UnicodeEmoji("\ud83d\udce7")

    /** Unicode emoji with a Discord shortcode of `:love_letter:`. Represented in Unicode as 💌. */
    object LoveLetter : UnicodeEmoji("\ud83d\udc8c")

    /** Unicode emoji with a Discord shortcode of `:inbox_tray:`. Represented in Unicode as 📥. */
    object InboxTray : UnicodeEmoji("\ud83d\udce5")

    /** Unicode emoji with a Discord shortcode of `:outbox_tray:`. Represented in Unicode as 📤. */
    object OutboxTray : UnicodeEmoji("\ud83d\udce4")

    /** Unicode emoji with a Discord shortcode of `:package:`. Represented in Unicode as 📦. */
    object Package : UnicodeEmoji("\ud83d\udce6")

    /** Unicode emoji with a Discord shortcode of `:label:`. Represented in Unicode as 🏷️. */
    object Label : UnicodeEmoji("\ud83c\udff7\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:mailbox_closed:`. Represented in Unicode as 📪. */
    object MailboxClosed : UnicodeEmoji("\ud83d\udcea")

    /** Unicode emoji with a Discord shortcode of `:mailbox:`. Represented in Unicode as 📫. */
    object Mailbox : UnicodeEmoji("\ud83d\udceb")

    /** Unicode emoji with a Discord shortcode of `:mailbox_with_mail:`. Represented in Unicode as 📬. */
    object MailboxWithMail : UnicodeEmoji("\ud83d\udcec")

    /** Unicode emoji with a Discord shortcode of `:mailbox_with_no_mail:`. Represented in Unicode as 📭. */
    object MailboxWithNoMail : UnicodeEmoji("\ud83d\udced")

    /** Unicode emoji with a Discord shortcode of `:postbox:`. Represented in Unicode as 📮. */
    object Postbox : UnicodeEmoji("\ud83d\udcee")

    /** Unicode emoji with a Discord shortcode of `:postal_horn:`. Represented in Unicode as 📯. */
    object PostalHorn : UnicodeEmoji("\ud83d\udcef")

    /** Unicode emoji with a Discord shortcode of `:scroll:`. Represented in Unicode as 📜. */
    object Scroll : UnicodeEmoji("\ud83d\udcdc")

    /** Unicode emoji with a Discord shortcode of `:page_with_curl:`. Represented in Unicode as 📃. */
    object PageWithCurl : UnicodeEmoji("\ud83d\udcc3")

    /** Unicode emoji with a Discord shortcode of `:page_facing_up:`. Represented in Unicode as 📄. */
    object PageFacingUp : UnicodeEmoji("\ud83d\udcc4")

    /** Unicode emoji with a Discord shortcode of `:bookmark_tabs:`. Represented in Unicode as 📑. */
    object BookmarkTabs : UnicodeEmoji("\ud83d\udcd1")

    /** Unicode emoji with a Discord shortcode of `:receipt:`. Represented in Unicode as 🧾. */
    object Receipt : UnicodeEmoji("\ud83e\uddfe")

    /** Unicode emoji with a Discord shortcode of `:bar_chart:`. Represented in Unicode as 📊. */
    object BarChart : UnicodeEmoji("\ud83d\udcca")

    /** Unicode emoji with a Discord shortcode of `:chart_with_upwards_trend:`. Represented in Unicode as 📈. */
    object ChartWithUpwardsTrend : UnicodeEmoji("\ud83d\udcc8")

    /** Unicode emoji with a Discord shortcode of `:chart_with_downwards_trend:`. Represented in Unicode as 📉. */
    object ChartWithDownwardsTrend : UnicodeEmoji("\ud83d\udcc9")

    /** Unicode emoji with Discord shortcodes of `:notepad_spiral:` and `:spiral_note_pad:`. Represented in Unicode as 🗒️. */
    object NotepadSpiral : UnicodeEmoji("\ud83d\uddd2\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:calendar_spiral:` and `:spiral_calendar_pad:`. Represented in Unicode as 🗓️. */
    object CalendarSpiral : UnicodeEmoji("\ud83d\uddd3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:calendar:`. Represented in Unicode as 📆. */
    object Calendar : UnicodeEmoji("\ud83d\udcc6")

    /** Unicode emoji with a Discord shortcode of `:date:`. Represented in Unicode as 📅. */
    object Date : UnicodeEmoji("\ud83d\udcc5")

    /** Unicode emoji with a Discord shortcode of `:wastebasket:`. Represented in Unicode as 🗑️. */
    object Wastebasket : UnicodeEmoji("\ud83d\uddd1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:card_index:`. Represented in Unicode as 📇. */
    object CardIndex : UnicodeEmoji("\ud83d\udcc7")

    /** Unicode emoji with Discord shortcodes of `:card_box:` and `:card_file_box:`. Represented in Unicode as 🗃️. */
    object CardBox : UnicodeEmoji("\ud83d\uddc3\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:ballot_box:` and `:ballot_box_with_ballot:`. Represented in Unicode as 🗳️. */
    object BallotBox : UnicodeEmoji("\ud83d\uddf3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:file_cabinet:`. Represented in Unicode as 🗄️. */
    object FileCabinet : UnicodeEmoji("\ud83d\uddc4\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:clipboard:`. Represented in Unicode as 📋. */
    object Clipboard : UnicodeEmoji("\ud83d\udccb")

    /** Unicode emoji with a Discord shortcode of `:file_folder:`. Represented in Unicode as 📁. */
    object FileFolder : UnicodeEmoji("\ud83d\udcc1")

    /** Unicode emoji with a Discord shortcode of `:open_file_folder:`. Represented in Unicode as 📂. */
    object OpenFileFolder : UnicodeEmoji("\ud83d\udcc2")

    /** Unicode emoji with Discord shortcodes of `:dividers:` and `:card_index_dividers:`. Represented in Unicode as 🗂️. */
    object Dividers : UnicodeEmoji("\ud83d\uddc2\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:newspaper2:` and `:rolled_up_newspaper:`. Represented in Unicode as 🗞️. */
    object Newspaper2 : UnicodeEmoji("\ud83d\uddde\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:newspaper:`. Represented in Unicode as 📰. */
    object Newspaper : UnicodeEmoji("\ud83d\udcf0")

    /** Unicode emoji with a Discord shortcode of `:notebook:`. Represented in Unicode as 📓. */
    object Notebook : UnicodeEmoji("\ud83d\udcd3")

    /** Unicode emoji with a Discord shortcode of `:notebook_with_decorative_cover:`. Represented in Unicode as 📔. */
    object NotebookWithDecorativeCover : UnicodeEmoji("\ud83d\udcd4")

    /** Unicode emoji with a Discord shortcode of `:ledger:`. Represented in Unicode as 📒. */
    object Ledger : UnicodeEmoji("\ud83d\udcd2")

    /** Unicode emoji with a Discord shortcode of `:closed_book:`. Represented in Unicode as 📕. */
    object ClosedBook : UnicodeEmoji("\ud83d\udcd5")

    /** Unicode emoji with a Discord shortcode of `:green_book:`. Represented in Unicode as 📗. */
    object GreenBook : UnicodeEmoji("\ud83d\udcd7")

    /** Unicode emoji with a Discord shortcode of `:blue_book:`. Represented in Unicode as 📘. */
    object BlueBook : UnicodeEmoji("\ud83d\udcd8")

    /** Unicode emoji with a Discord shortcode of `:orange_book:`. Represented in Unicode as 📙. */
    object OrangeBook : UnicodeEmoji("\ud83d\udcd9")

    /** Unicode emoji with a Discord shortcode of `:books:`. Represented in Unicode as 📚. */
    object Books : UnicodeEmoji("\ud83d\udcda")

    /** Unicode emoji with a Discord shortcode of `:book:`. Represented in Unicode as 📖. */
    object Book : UnicodeEmoji("\ud83d\udcd6")

    /** Unicode emoji with a Discord shortcode of `:bookmark:`. Represented in Unicode as 🔖. */
    object Bookmark : UnicodeEmoji("\ud83d\udd16")

    /** Unicode emoji with a Discord shortcode of `:safety_pin:`. Represented in Unicode as 🧷. */
    object SafetyPin : UnicodeEmoji("\ud83e\uddf7")

    /** Unicode emoji with a Discord shortcode of `:link:`. Represented in Unicode as 🔗. */
    object Link : UnicodeEmoji("\ud83d\udd17")

    /** Unicode emoji with a Discord shortcode of `:paperclip:`. Represented in Unicode as 📎. */
    object Paperclip : UnicodeEmoji("\ud83d\udcce")

    /** Unicode emoji with Discord shortcodes of `:paperclips:` and `:linked_paperclips:`. Represented in Unicode as 🖇️. */
    object Paperclips : UnicodeEmoji("\ud83d\udd87\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:triangular_ruler:`. Represented in Unicode as 📐. */
    object TriangularRuler : UnicodeEmoji("\ud83d\udcd0")

    /** Unicode emoji with a Discord shortcode of `:straight_ruler:`. Represented in Unicode as 📏. */
    object StraightRuler : UnicodeEmoji("\ud83d\udccf")

    /** Unicode emoji with a Discord shortcode of `:abacus:`. Represented in Unicode as 🧮. */
    object Abacus : UnicodeEmoji("\ud83e\uddee")

    /** Unicode emoji with a Discord shortcode of `:pushpin:`. Represented in Unicode as 📌. */
    object Pushpin : UnicodeEmoji("\ud83d\udccc")

    /** Unicode emoji with a Discord shortcode of `:round_pushpin:`. Represented in Unicode as 📍. */
    object RoundPushpin : UnicodeEmoji("\ud83d\udccd")

    /** Unicode emoji with a Discord shortcode of `:scissors:`. Represented in Unicode as ✂️. */
    object Scissors : UnicodeEmoji("\u2702\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:pen_ballpoint:` and `:lower_left_ballpoint_pen:`. Represented in Unicode as 🖊️. */
    object PenBallpoint : UnicodeEmoji("\ud83d\udd8a\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:pen_fountain:` and `:lower_left_fountain_pen:`. Represented in Unicode as 🖋️. */
    object PenFountain : UnicodeEmoji("\ud83d\udd8b\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:black_nib:`. Represented in Unicode as ✒️. */
    object BlackNib : UnicodeEmoji("\u2712\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:paintbrush:` and `:lower_left_paintbrush:`. Represented in Unicode as 🖌️. */
    object Paintbrush : UnicodeEmoji("\ud83d\udd8c\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:crayon:` and `:lower_left_crayon:`. Represented in Unicode as 🖍️. */
    object Crayon : UnicodeEmoji("\ud83d\udd8d\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:memo:` and `:pencil:`. Represented in Unicode as 📝. */
    object Memo : UnicodeEmoji("\ud83d\udcdd")

    /** Unicode emoji with a Discord shortcode of `:pencil2:`. Represented in Unicode as ✏️. */
    object Pencil2 : UnicodeEmoji("\u270f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:mag:`. Represented in Unicode as 🔍. */
    object Mag : UnicodeEmoji("\ud83d\udd0d")

    /** Unicode emoji with a Discord shortcode of `:mag_right:`. Represented in Unicode as 🔎. */
    object MagRight : UnicodeEmoji("\ud83d\udd0e")

    /** Unicode emoji with a Discord shortcode of `:lock_with_ink_pen:`. Represented in Unicode as 🔏. */
    object LockWithInkPen : UnicodeEmoji("\ud83d\udd0f")

    /** Unicode emoji with a Discord shortcode of `:closed_lock_with_key:`. Represented in Unicode as 🔐. */
    object ClosedLockWithKey : UnicodeEmoji("\ud83d\udd10")

    /** Unicode emoji with a Discord shortcode of `:lock:`. Represented in Unicode as 🔒. */
    object Lock : UnicodeEmoji("\ud83d\udd12")

    /** Unicode emoji with a Discord shortcode of `:unlock:`. Represented in Unicode as 🔓. */
    object Unlock : UnicodeEmoji("\ud83d\udd13")

    /** Unicode emoji with a Discord shortcode of `:heart:`. Represented in Unicode as ❤️. */
    object Heart : UnicodeEmoji("\u2764\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:orange_heart:`. Represented in Unicode as 🧡. */
    object OrangeHeart : UnicodeEmoji("\ud83e\udde1")

    /** Unicode emoji with a Discord shortcode of `:yellow_heart:`. Represented in Unicode as 💛. */
    object YellowHeart : UnicodeEmoji("\ud83d\udc9b")

    /** Unicode emoji with a Discord shortcode of `:green_heart:`. Represented in Unicode as 💚. */
    object GreenHeart : UnicodeEmoji("\ud83d\udc9a")

    /** Unicode emoji with a Discord shortcode of `:blue_heart:`. Represented in Unicode as 💙. */
    object BlueHeart : UnicodeEmoji("\ud83d\udc99")

    /** Unicode emoji with a Discord shortcode of `:purple_heart:`. Represented in Unicode as 💜. */
    object PurpleHeart : UnicodeEmoji("\ud83d\udc9c")

    /** Unicode emoji with a Discord shortcode of `:black_heart:`. Represented in Unicode as 🖤. */
    object BlackHeart : UnicodeEmoji("\ud83d\udda4")

    /** Unicode emoji with a Discord shortcode of `:brown_heart:`. Represented in Unicode as 🤎. */
    object BrownHeart : UnicodeEmoji("\ud83e\udd0e")

    /** Unicode emoji with a Discord shortcode of `:white_heart:`. Represented in Unicode as 🤍. */
    object WhiteHeart : UnicodeEmoji("\ud83e\udd0d")

    /** Unicode emoji with a Discord shortcode of `:broken_heart:`. Represented in Unicode as 💔. */
    object BrokenHeart : UnicodeEmoji("\ud83d\udc94")

    /** Unicode emoji with Discord shortcodes of `:heart_exclamation:` and `:heavy_heart_exclamation_mark_ornament:`. Represented in Unicode as ❣️. */
    object HeartExclamation : UnicodeEmoji("\u2763\ufe0f")

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

    /** Unicode emoji with Discord shortcodes of `:peace:` and `:peace_symbol:`. Represented in Unicode as ☮️. */
    object Peace : UnicodeEmoji("\u262e\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:cross:` and `:latin_cross:`. Represented in Unicode as ✝️. */
    object Cross : UnicodeEmoji("\u271d\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:star_and_crescent:`. Represented in Unicode as ☪️. */
    object StarAndCrescent : UnicodeEmoji("\u262a\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:om_symbol:`. Represented in Unicode as 🕉️. */
    object OmSymbol : UnicodeEmoji("\ud83d\udd49\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:wheel_of_dharma:`. Represented in Unicode as ☸️. */
    object WheelOfDharma : UnicodeEmoji("\u2638\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:star_of_david:`. Represented in Unicode as ✡️. */
    object StarOfDavid : UnicodeEmoji("\u2721\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:six_pointed_star:`. Represented in Unicode as 🔯. */
    object SixPointedStar : UnicodeEmoji("\ud83d\udd2f")

    /** Unicode emoji with a Discord shortcode of `:menorah:`. Represented in Unicode as 🕎. */
    object Menorah : UnicodeEmoji("\ud83d\udd4e")

    /** Unicode emoji with a Discord shortcode of `:yin_yang:`. Represented in Unicode as ☯️. */
    object YinYang : UnicodeEmoji("\u262f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:orthodox_cross:`. Represented in Unicode as ☦️. */
    object OrthodoxCross : UnicodeEmoji("\u2626\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:worship_symbol:` and `:place_of_worship:`. Represented in Unicode as 🛐. */
    object WorshipSymbol : UnicodeEmoji("\ud83d\uded0")

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

    /** Unicode emoji with Discord shortcodes of `:atom:` and `:atom_symbol:`. Represented in Unicode as ⚛️. */
    object Atom : UnicodeEmoji("\u269b\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:accept:`. Represented in Unicode as 🉑. */
    object Accept : UnicodeEmoji("\ud83c\ude51")

    /** Unicode emoji with Discord shortcodes of `:radioactive:` and `:radioactive_sign:`. Represented in Unicode as ☢️. */
    object Radioactive : UnicodeEmoji("\u2622\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:biohazard:` and `:biohazard_sign:`. Represented in Unicode as ☣️. */
    object Biohazard : UnicodeEmoji("\u2623\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:u6708:`. Represented in Unicode as 🈷️. */
    object U6708 : UnicodeEmoji("\ud83c\ude37\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:eight_pointed_black_star:`. Represented in Unicode as ✴️. */
    object EightPointedBlackStar : UnicodeEmoji("\u2734\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:vs:`. Represented in Unicode as 🆚. */
    object VS : UnicodeEmoji("\ud83c\udd9a")

    /** Unicode emoji with a Discord shortcode of `:white_flower:`. Represented in Unicode as 💮. */
    object WhiteFlower : UnicodeEmoji("\ud83d\udcae")

    /** Unicode emoji with a Discord shortcode of `:ideograph_advantage:`. Represented in Unicode as 🉐. */
    object IdeographAdvantage : UnicodeEmoji("\ud83c\ude50")

    /** Unicode emoji with a Discord shortcode of `:secret:`. Represented in Unicode as ㊙️. */
    object Secret : UnicodeEmoji("\u3299\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:congratulations:`. Represented in Unicode as ㊗️. */
    object Congratulations : UnicodeEmoji("\u3297\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:u5408:`. Represented in Unicode as 🈴. */
    object U5408 : UnicodeEmoji("\ud83c\ude34")

    /** Unicode emoji with a Discord shortcode of `:u6e80:`. Represented in Unicode as 🈵. */
    object U6e80 : UnicodeEmoji("\ud83c\ude35")

    /** Unicode emoji with a Discord shortcode of `:u5272:`. Represented in Unicode as 🈹. */
    object U5272 : UnicodeEmoji("\ud83c\ude39")

    /** Unicode emoji with a Discord shortcode of `:u7981:`. Represented in Unicode as 🈲. */
    object U7981 : UnicodeEmoji("\ud83c\ude32")

    /** Unicode emoji with a Discord shortcode of `:a:`. Represented in Unicode as 🅰️. */
    object A : UnicodeEmoji("\ud83c\udd70\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:b:`. Represented in Unicode as 🅱️. */
    object B : UnicodeEmoji("\ud83c\udd71\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ab:`. Represented in Unicode as 🆎. */
    object AB : UnicodeEmoji("\ud83c\udd8e")

    /** Unicode emoji with a Discord shortcode of `:cl:`. Represented in Unicode as 🆑. */
    object CL : UnicodeEmoji("\ud83c\udd91")

    /** Unicode emoji with a Discord shortcode of `:o2:`. Represented in Unicode as 🅾️. */
    object O2 : UnicodeEmoji("\ud83c\udd7e\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:sos:`. Represented in Unicode as 🆘. */
    object Sos : UnicodeEmoji("\ud83c\udd98")

    /** Unicode emoji with a Discord shortcode of `:x:`. Represented in Unicode as ❌. */
    object X : UnicodeEmoji("\u274c")

    /** Unicode emoji with a Discord shortcode of `:o:`. Represented in Unicode as ⭕. */
    object O : UnicodeEmoji("\u2b55")

    /** Unicode emoji with Discord shortcodes of `:stop_sign:` and `:octagonal_sign:`. Represented in Unicode as 🛑. */
    object StopSign : UnicodeEmoji("\ud83d\uded1")

    /** Unicode emoji with a Discord shortcode of `:no_entry:`. Represented in Unicode as ⛔. */
    object NoEntry : UnicodeEmoji("\u26d4")

    /** Unicode emoji with a Discord shortcode of `:name_badge:`. Represented in Unicode as 📛. */
    object NameBadge : UnicodeEmoji("\ud83d\udcdb")

    /** Unicode emoji with a Discord shortcode of `:no_entry_sign:`. Represented in Unicode as 🚫. */
    object NoEntrySign : UnicodeEmoji("\ud83d\udeab")

    /** Unicode emoji with a Discord shortcode of `:100:`. Represented in Unicode as 💯. */
    object OneHundred : UnicodeEmoji("\ud83d\udcaf")

    /** Unicode emoji with a Discord shortcode of `:anger:`. Represented in Unicode as 💢. */
    object Anger : UnicodeEmoji("\ud83d\udca2")

    /** Unicode emoji with a Discord shortcode of `:hotsprings:`. Represented in Unicode as ♨️. */
    object Hotsprings : UnicodeEmoji("\u2668\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:no_smoking:`. Represented in Unicode as 🚭. */
    object NoSmoking : UnicodeEmoji("\ud83d\udead")

    /** Unicode emoji with a Discord shortcode of `:exclamation:`. Represented in Unicode as ❗. */
    object Exclamation : UnicodeEmoji("\u2757")

    /** Unicode emoji with a Discord shortcode of `:grey_exclamation:`. Represented in Unicode as ❕. */
    object GreyExclamation : UnicodeEmoji("\u2755")

    /** Unicode emoji with a Discord shortcode of `:question:`. Represented in Unicode as ❓. */
    object Question : UnicodeEmoji("\u2753")

    /** Unicode emoji with a Discord shortcode of `:grey_question:`. Represented in Unicode as ❔. */
    object GreyQuestion : UnicodeEmoji("\u2754")

    /** Unicode emoji with a Discord shortcode of `:bangbang:`. Represented in Unicode as ‼️. */
    object Bangbang : UnicodeEmoji("\u203c\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:interrobang:`. Represented in Unicode as ⁉️. */
    object Interrobang : UnicodeEmoji("\u2049\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:low_brightness:`. Represented in Unicode as 🔅. */
    object LowBrightness : UnicodeEmoji("\ud83d\udd05")

    /** Unicode emoji with a Discord shortcode of `:high_brightness:`. Represented in Unicode as 🔆. */
    object HighBrightness : UnicodeEmoji("\ud83d\udd06")

    /** Unicode emoji with a Discord shortcode of `:part_alternation_mark:`. Represented in Unicode as 〽️. */
    object PartAlternationMark : UnicodeEmoji("\u303d\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:warning:`. Represented in Unicode as ⚠️. */
    object Warning : UnicodeEmoji("\u26a0\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:children_crossing:`. Represented in Unicode as 🚸. */
    object ChildrenCrossing : UnicodeEmoji("\ud83d\udeb8")

    /** Unicode emoji with a Discord shortcode of `:trident:`. Represented in Unicode as 🔱. */
    object Trident : UnicodeEmoji("\ud83d\udd31")

    /** Unicode emoji with a Discord shortcode of `:fleur_de_lis:`. Represented in Unicode as ⚜️. */
    object FleurDeLis : UnicodeEmoji("\u269c\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:beginner:`. Represented in Unicode as 🔰. */
    object Beginner : UnicodeEmoji("\ud83d\udd30")

    /** Unicode emoji with a Discord shortcode of `:recycle:`. Represented in Unicode as ♻️. */
    object Recycle : UnicodeEmoji("\u267b\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:white_check_mark:`. Represented in Unicode as ✅. */
    object WhiteCheckMark : UnicodeEmoji("\u2705")

    /** Unicode emoji with a Discord shortcode of `:u6307:`. Represented in Unicode as 🈯. */
    object U6307 : UnicodeEmoji("\ud83c\ude2f")

    /** Unicode emoji with a Discord shortcode of `:chart:`. Represented in Unicode as 💹. */
    object Chart : UnicodeEmoji("\ud83d\udcb9")

    /** Unicode emoji with a Discord shortcode of `:sparkle:`. Represented in Unicode as ❇️. */
    object Sparkle : UnicodeEmoji("\u2747\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:eight_spoked_asterisk:`. Represented in Unicode as ✳️. */
    object EightSpokedAsterisk : UnicodeEmoji("\u2733\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:negative_squared_cross_mark:`. Represented in Unicode as ❎. */
    object NegativeSquaredCrossMark : UnicodeEmoji("\u274e")

    /** Unicode emoji with a Discord shortcode of `:globe_with_meridians:`. Represented in Unicode as 🌐. */
    object GlobeWithMeridians : UnicodeEmoji("\ud83c\udf10")

    /** Unicode emoji with a Discord shortcode of `:diamond_shape_with_a_dot_inside:`. Represented in Unicode as 💠. */
    object DiamondShapeWithADotInside : UnicodeEmoji("\ud83d\udca0")

    /** Unicode emoji with a Discord shortcode of `:m:`. Represented in Unicode as Ⓜ️. */
    object M : UnicodeEmoji("\u24c2\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:cyclone:`. Represented in Unicode as 🌀. */
    object Cyclone : UnicodeEmoji("\ud83c\udf00")

    /** Unicode emoji with a Discord shortcode of `:zzz:`. Represented in Unicode as 💤. */
    object Zzz : UnicodeEmoji("\ud83d\udca4")

    /** Unicode emoji with a Discord shortcode of `:atm:`. Represented in Unicode as 🏧. */
    object Atm : UnicodeEmoji("\ud83c\udfe7")

    /** Unicode emoji with a Discord shortcode of `:wc:`. Represented in Unicode as 🚾. */
    object WC : UnicodeEmoji("\ud83d\udebe")

    /** Unicode emoji with a Discord shortcode of `:wheelchair:`. Represented in Unicode as ♿. */
    object Wheelchair : UnicodeEmoji("\u267f")

    /** Unicode emoji with a Discord shortcode of `:parking:`. Represented in Unicode as 🅿️. */
    object Parking : UnicodeEmoji("\ud83c\udd7f\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:u7a7a:`. Represented in Unicode as 🈳. */
    object U7a7a : UnicodeEmoji("\ud83c\ude33")

    /** Unicode emoji with a Discord shortcode of `:sa:`. Represented in Unicode as 🈂️. */
    object SA : UnicodeEmoji("\ud83c\ude02\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:passport_control:`. Represented in Unicode as 🛂. */
    object PassportControl : UnicodeEmoji("\ud83d\udec2")

    /** Unicode emoji with a Discord shortcode of `:customs:`. Represented in Unicode as 🛃. */
    object Customs : UnicodeEmoji("\ud83d\udec3")

    /** Unicode emoji with a Discord shortcode of `:baggage_claim:`. Represented in Unicode as 🛄. */
    object BaggageClaim : UnicodeEmoji("\ud83d\udec4")

    /** Unicode emoji with a Discord shortcode of `:left_luggage:`. Represented in Unicode as 🛅. */
    object LeftLuggage : UnicodeEmoji("\ud83d\udec5")

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

    /** Unicode emoji with a Discord shortcode of `:symbols:`. Represented in Unicode as 🔣. */
    object Symbols : UnicodeEmoji("\ud83d\udd23")

    /** Unicode emoji with a Discord shortcode of `:information_source:`. Represented in Unicode as ℹ️. */
    object InformationSource : UnicodeEmoji("\u2139\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:abc:`. Represented in Unicode as 🔤. */
    object Abc : UnicodeEmoji("\ud83d\udd24")

    /** Unicode emoji with a Discord shortcode of `:abcd:`. Represented in Unicode as 🔡. */
    object Abcd : UnicodeEmoji("\ud83d\udd21")

    /** Unicode emoji with a Discord shortcode of `:capital_abcd:`. Represented in Unicode as 🔠. */
    object CapitalAbcd : UnicodeEmoji("\ud83d\udd20")

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

    /** Unicode emoji with a Discord shortcode of `:zero:`. Represented in Unicode as 0️⃣. */
    object Zero : UnicodeEmoji("\u0030\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:one:`. Represented in Unicode as 1️⃣. */
    object One : UnicodeEmoji("\u0031\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:two:`. Represented in Unicode as 2️⃣. */
    object Two : UnicodeEmoji("\u0032\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:three:`. Represented in Unicode as 3️⃣. */
    object Three : UnicodeEmoji("\u0033\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:four:`. Represented in Unicode as 4️⃣. */
    object Four : UnicodeEmoji("\u0034\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:five:`. Represented in Unicode as 5️⃣. */
    object Five : UnicodeEmoji("\u0035\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:six:`. Represented in Unicode as 6️⃣. */
    object Six : UnicodeEmoji("\u0036\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:seven:`. Represented in Unicode as 7️⃣. */
    object Seven : UnicodeEmoji("\u0037\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:eight:`. Represented in Unicode as 8️⃣. */
    object Eight : UnicodeEmoji("\u0038\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:nine:`. Represented in Unicode as 9️⃣. */
    object Nine : UnicodeEmoji("\u0039\ufe0f\u20e3")

    /** Unicode emoji with a Discord shortcode of `:keycap_ten:`. Represented in Unicode as 🔟. */
    object KeycapTen : UnicodeEmoji("\ud83d\udd1f")

    /** Unicode emoji with a Discord shortcode of `:1234:`. Represented in Unicode as 🔢. */
    object OneTwoThreeFour : UnicodeEmoji("\ud83d\udd22")

    /** Unicode emoji with a Discord shortcode of `:hash:`. Represented in Unicode as #️⃣. */
    object Hash : UnicodeEmoji("\u0023\ufe0f\u20e3")

    /** Unicode emoji with Discord shortcodes of `:asterisk:` and `:keycap_asterisk:`. Represented in Unicode as *️⃣. */
    object Asterisk : UnicodeEmoji("\u002a\ufe0f\u20e3")

    /** Unicode emoji with Discord shortcodes of `:eject:` and `:eject_symbol:`. Represented in Unicode as ⏏️. */
    object Eject : UnicodeEmoji("\u23cf\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_forward:`. Represented in Unicode as ▶️. */
    object ArrowForward : UnicodeEmoji("\u25b6\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:pause_button:` and `:double_vertical_bar:`. Represented in Unicode as ⏸️. */
    object PauseButton : UnicodeEmoji("\u23f8\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:play_pause:`. Represented in Unicode as ⏯️. */
    object PlayPause : UnicodeEmoji("\u23ef\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:stop_button:`. Represented in Unicode as ⏹️. */
    object StopButton : UnicodeEmoji("\u23f9\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:record_button:`. Represented in Unicode as ⏺️. */
    object RecordButton : UnicodeEmoji("\u23fa\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:track_next:` and `:next_track:`. Represented in Unicode as ⏭️. */
    object TrackNext : UnicodeEmoji("\u23ed\ufe0f")

    /** Unicode emoji with Discord shortcodes of `:track_previous:` and `:previous_track:`. Represented in Unicode as ⏮️. */
    object TrackPrevious : UnicodeEmoji("\u23ee\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:fast_forward:`. Represented in Unicode as ⏩. */
    object FastForward : UnicodeEmoji("\u23e9")

    /** Unicode emoji with a Discord shortcode of `:rewind:`. Represented in Unicode as ⏪. */
    object Rewind : UnicodeEmoji("\u23ea")

    /** Unicode emoji with a Discord shortcode of `:arrow_double_up:`. Represented in Unicode as ⏫. */
    object ArrowDoubleUp : UnicodeEmoji("\u23eb")

    /** Unicode emoji with a Discord shortcode of `:arrow_double_down:`. Represented in Unicode as ⏬. */
    object ArrowDoubleDown : UnicodeEmoji("\u23ec")

    /** Unicode emoji with a Discord shortcode of `:arrow_backward:`. Represented in Unicode as ◀️. */
    object ArrowBackward : UnicodeEmoji("\u25c0\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_up_small:`. Represented in Unicode as 🔼. */
    object ArrowUpSmall : UnicodeEmoji("\ud83d\udd3c")

    /** Unicode emoji with a Discord shortcode of `:arrow_down_small:`. Represented in Unicode as 🔽. */
    object ArrowDownSmall : UnicodeEmoji("\ud83d\udd3d")

    /** Unicode emoji with a Discord shortcode of `:arrow_right:`. Represented in Unicode as ➡️. */
    object ArrowRight : UnicodeEmoji("\u27a1\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_left:`. Represented in Unicode as ⬅️. */
    object ArrowLeft : UnicodeEmoji("\u2b05\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_up:`. Represented in Unicode as ⬆️. */
    object ArrowUp : UnicodeEmoji("\u2b06\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_down:`. Represented in Unicode as ⬇️. */
    object ArrowDown : UnicodeEmoji("\u2b07\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_upper_right:`. Represented in Unicode as ↗️. */
    object ArrowUpperRight : UnicodeEmoji("\u2197\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_lower_right:`. Represented in Unicode as ↘️. */
    object ArrowLowerRight : UnicodeEmoji("\u2198\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_lower_left:`. Represented in Unicode as ↙️. */
    object ArrowLowerLeft : UnicodeEmoji("\u2199\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_upper_left:`. Represented in Unicode as ↖️. */
    object ArrowUpperLeft : UnicodeEmoji("\u2196\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_up_down:`. Represented in Unicode as ↕️. */
    object ArrowUpDown : UnicodeEmoji("\u2195\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:left_right_arrow:`. Represented in Unicode as ↔️. */
    object LeftRightArrow : UnicodeEmoji("\u2194\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_right_hook:`. Represented in Unicode as ↪️. */
    object ArrowRightHook : UnicodeEmoji("\u21aa\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:leftwards_arrow_with_hook:`. Represented in Unicode as ↩️. */
    object LeftwardsArrowWithHook : UnicodeEmoji("\u21a9\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_heading_up:`. Represented in Unicode as ⤴️. */
    object ArrowHeadingUp : UnicodeEmoji("\u2934\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:arrow_heading_down:`. Represented in Unicode as ⤵️. */
    object ArrowHeadingDown : UnicodeEmoji("\u2935\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:twisted_rightwards_arrows:`. Represented in Unicode as 🔀. */
    object TwistedRightwardsArrows : UnicodeEmoji("\ud83d\udd00")

    /** Unicode emoji with a Discord shortcode of `:repeat:`. Represented in Unicode as 🔁. */
    object Repeat : UnicodeEmoji("\ud83d\udd01")

    /** Unicode emoji with a Discord shortcode of `:repeat_one:`. Represented in Unicode as 🔂. */
    object RepeatOne : UnicodeEmoji("\ud83d\udd02")

    /** Unicode emoji with a Discord shortcode of `:arrows_counterclockwise:`. Represented in Unicode as 🔄. */
    object ArrowsCounterclockwise : UnicodeEmoji("\ud83d\udd04")

    /** Unicode emoji with a Discord shortcode of `:arrows_clockwise:`. Represented in Unicode as 🔃. */
    object ArrowsClockwise : UnicodeEmoji("\ud83d\udd03")

    /** Unicode emoji with a Discord shortcode of `:musical_note:`. Represented in Unicode as 🎵. */
    object MusicalNote : UnicodeEmoji("\ud83c\udfb5")

    /** Unicode emoji with a Discord shortcode of `:notes:`. Represented in Unicode as 🎶. */
    object Notes : UnicodeEmoji("\ud83c\udfb6")

    /** Unicode emoji with a Discord shortcode of `:heavy_plus_sign:`. Represented in Unicode as ➕. */
    object HeavyPlusSign : UnicodeEmoji("\u2795")

    /** Unicode emoji with a Discord shortcode of `:heavy_minus_sign:`. Represented in Unicode as ➖. */
    object HeavyMinusSign : UnicodeEmoji("\u2796")

    /** Unicode emoji with a Discord shortcode of `:heavy_division_sign:`. Represented in Unicode as ➗. */
    object HeavyDivisionSign : UnicodeEmoji("\u2797")

    /** Unicode emoji with a Discord shortcode of `:heavy_multiplication_x:`. Represented in Unicode as ✖️. */
    object HeavyMultiplicationX : UnicodeEmoji("\u2716\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:infinity:`. Represented in Unicode as ♾️. */
    object Infinity : UnicodeEmoji("\u267e\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:heavy_dollar_sign:`. Represented in Unicode as 💲. */
    object HeavyDollarSign : UnicodeEmoji("\ud83d\udcb2")

    /** Unicode emoji with a Discord shortcode of `:currency_exchange:`. Represented in Unicode as 💱. */
    object CurrencyExchange : UnicodeEmoji("\ud83d\udcb1")

    /** Unicode emoji with a Discord shortcode of `:tm:`. Represented in Unicode as ™️. */
    object TM : UnicodeEmoji("\u2122\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:copyright:`. Represented in Unicode as ©️. */
    object Copyright : UnicodeEmoji("\u00a9\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:registered:`. Represented in Unicode as ®️. */
    object Registered : UnicodeEmoji("\u00ae\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:wavy_dash:`. Represented in Unicode as 〰️. */
    object WavyDash : UnicodeEmoji("\u3030\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:curly_loop:`. Represented in Unicode as ➰. */
    object CurlyLoop : UnicodeEmoji("\u27b0")

    /** Unicode emoji with a Discord shortcode of `:loop:`. Represented in Unicode as ➿. */
    object Loop : UnicodeEmoji("\u27bf")

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

    /** Unicode emoji with a Discord shortcode of `:heavy_check_mark:`. Represented in Unicode as ✔️. */
    object HeavyCheckMark : UnicodeEmoji("\u2714\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:ballot_box_with_check:`. Represented in Unicode as ☑️. */
    object BallotBoxWithCheck : UnicodeEmoji("\u2611\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:radio_button:`. Represented in Unicode as 🔘. */
    object RadioButton : UnicodeEmoji("\ud83d\udd18")

    /** Unicode emoji with a Discord shortcode of `:white_circle:`. Represented in Unicode as ⚪. */
    object WhiteCircle : UnicodeEmoji("\u26aa")

    /** Unicode emoji with a Discord shortcode of `:black_circle:`. Represented in Unicode as ⚫. */
    object BlackCircle : UnicodeEmoji("\u26ab")

    /** Unicode emoji with a Discord shortcode of `:red_circle:`. Represented in Unicode as 🔴. */
    object RedCircle : UnicodeEmoji("\ud83d\udd34")

    /** Unicode emoji with a Discord shortcode of `:blue_circle:`. Represented in Unicode as 🔵. */
    object BlueCircle : UnicodeEmoji("\ud83d\udd35")

    /** Unicode emoji with a Discord shortcode of `:brown_circle:`. Represented in Unicode as 🟤. */
    object BrownCircle : UnicodeEmoji("\ud83d\udfe4")

    /** Unicode emoji with a Discord shortcode of `:purple_circle:`. Represented in Unicode as 🟣. */
    object PurpleCircle : UnicodeEmoji("\ud83d\udfe3")

    /** Unicode emoji with a Discord shortcode of `:green_circle:`. Represented in Unicode as 🟢. */
    object GreenCircle : UnicodeEmoji("\ud83d\udfe2")

    /** Unicode emoji with a Discord shortcode of `:yellow_circle:`. Represented in Unicode as 🟡. */
    object YellowCircle : UnicodeEmoji("\ud83d\udfe1")

    /** Unicode emoji with a Discord shortcode of `:orange_circle:`. Represented in Unicode as 🟠. */
    object OrangeCircle : UnicodeEmoji("\ud83d\udfe0")

    /** Unicode emoji with a Discord shortcode of `:small_red_triangle:`. Represented in Unicode as 🔺. */
    object SmallRedTriangle : UnicodeEmoji("\ud83d\udd3a")

    /** Unicode emoji with a Discord shortcode of `:small_red_triangle_down:`. Represented in Unicode as 🔻. */
    object SmallRedTriangleDown : UnicodeEmoji("\ud83d\udd3b")

    /** Unicode emoji with a Discord shortcode of `:small_orange_diamond:`. Represented in Unicode as 🔸. */
    object SmallOrangeDiamond : UnicodeEmoji("\ud83d\udd38")

    /** Unicode emoji with a Discord shortcode of `:small_blue_diamond:`. Represented in Unicode as 🔹. */
    object SmallBlueDiamond : UnicodeEmoji("\ud83d\udd39")

    /** Unicode emoji with a Discord shortcode of `:large_orange_diamond:`. Represented in Unicode as 🔶. */
    object LargeOrangeDiamond : UnicodeEmoji("\ud83d\udd36")

    /** Unicode emoji with a Discord shortcode of `:large_blue_diamond:`. Represented in Unicode as 🔷. */
    object LargeBlueDiamond : UnicodeEmoji("\ud83d\udd37")

    /** Unicode emoji with a Discord shortcode of `:white_square_button:`. Represented in Unicode as 🔳. */
    object WhiteSquareButton : UnicodeEmoji("\ud83d\udd33")

    /** Unicode emoji with a Discord shortcode of `:black_square_button:`. Represented in Unicode as 🔲. */
    object BlackSquareButton : UnicodeEmoji("\ud83d\udd32")

    /** Unicode emoji with a Discord shortcode of `:black_small_square:`. Represented in Unicode as ▪️. */
    object BlackSmallSquare : UnicodeEmoji("\u25aa\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:white_small_square:`. Represented in Unicode as ▫️. */
    object WhiteSmallSquare : UnicodeEmoji("\u25ab\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:black_medium_small_square:`. Represented in Unicode as ◾. */
    object BlackMediumSmallSquare : UnicodeEmoji("\u25fe")

    /** Unicode emoji with a Discord shortcode of `:white_medium_small_square:`. Represented in Unicode as ◽. */
    object WhiteMediumSmallSquare : UnicodeEmoji("\u25fd")

    /** Unicode emoji with a Discord shortcode of `:black_medium_square:`. Represented in Unicode as ◼️. */
    object BlackMediumSquare : UnicodeEmoji("\u25fc\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:white_medium_square:`. Represented in Unicode as ◻️. */
    object WhiteMediumSquare : UnicodeEmoji("\u25fb\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:black_large_square:`. Represented in Unicode as ⬛. */
    object BlackLargeSquare : UnicodeEmoji("\u2b1b")

    /** Unicode emoji with a Discord shortcode of `:white_large_square:`. Represented in Unicode as ⬜. */
    object WhiteLargeSquare : UnicodeEmoji("\u2b1c")

    /** Unicode emoji with a Discord shortcode of `:orange_square:`. Represented in Unicode as 🟧. */
    object OrangeSquare : UnicodeEmoji("\ud83d\udfe7")

    /** Unicode emoji with a Discord shortcode of `:blue_square:`. Represented in Unicode as 🟦. */
    object BlueSquare : UnicodeEmoji("\ud83d\udfe6")

    /** Unicode emoji with a Discord shortcode of `:red_square:`. Represented in Unicode as 🟥. */
    object RedSquare : UnicodeEmoji("\ud83d\udfe5")

    /** Unicode emoji with a Discord shortcode of `:brown_square:`. Represented in Unicode as 🟫. */
    object BrownSquare : UnicodeEmoji("\ud83d\udfeb")

    /** Unicode emoji with a Discord shortcode of `:purple_square:`. Represented in Unicode as 🟪. */
    object PurpleSquare : UnicodeEmoji("\ud83d\udfea")

    /** Unicode emoji with a Discord shortcode of `:green_square:`. Represented in Unicode as 🟩. */
    object GreenSquare : UnicodeEmoji("\ud83d\udfe9")

    /** Unicode emoji with a Discord shortcode of `:yellow_square:`. Represented in Unicode as 🟨. */
    object YellowSquare : UnicodeEmoji("\ud83d\udfe8")

    /** Unicode emoji with a Discord shortcode of `:speaker:`. Represented in Unicode as 🔈. */
    object Speaker : UnicodeEmoji("\ud83d\udd08")

    /** Unicode emoji with a Discord shortcode of `:mute:`. Represented in Unicode as 🔇. */
    object Mute : UnicodeEmoji("\ud83d\udd07")

    /** Unicode emoji with a Discord shortcode of `:sound:`. Represented in Unicode as 🔉. */
    object Sound : UnicodeEmoji("\ud83d\udd09")

    /** Unicode emoji with a Discord shortcode of `:loud_sound:`. Represented in Unicode as 🔊. */
    object LoudSound : UnicodeEmoji("\ud83d\udd0a")

    /** Unicode emoji with a Discord shortcode of `:bell:`. Represented in Unicode as 🔔. */
    object Bell : UnicodeEmoji("\ud83d\udd14")

    /** Unicode emoji with a Discord shortcode of `:no_bell:`. Represented in Unicode as 🔕. */
    object NoBell : UnicodeEmoji("\ud83d\udd15")

    /** Unicode emoji with a Discord shortcode of `:mega:`. Represented in Unicode as 📣. */
    object Mega : UnicodeEmoji("\ud83d\udce3")

    /** Unicode emoji with a Discord shortcode of `:loudspeaker:`. Represented in Unicode as 📢. */
    object Loudspeaker : UnicodeEmoji("\ud83d\udce2")

    /** Unicode emoji with Discord shortcodes of `:speech_left:` and `:left_speech_bubble:`. Represented in Unicode as 🗨️. */
    object SpeechLeft : UnicodeEmoji("\ud83d\udde8\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:eye_in_speech_bubble:`. Represented in Unicode as 👁‍🗨. */
    object EyeInSpeechBubble : UnicodeEmoji("\ud83d\udc41\u200d\ud83d\udde8")

    /** Unicode emoji with a Discord shortcode of `:speech_balloon:`. Represented in Unicode as 💬. */
    object SpeechBalloon : UnicodeEmoji("\ud83d\udcac")

    /** Unicode emoji with a Discord shortcode of `:thought_balloon:`. Represented in Unicode as 💭. */
    object ThoughtBalloon : UnicodeEmoji("\ud83d\udcad")

    /** Unicode emoji with Discord shortcodes of `:anger_right:` and `:right_anger_bubble:`. Represented in Unicode as 🗯️. */
    object AngerRight : UnicodeEmoji("\ud83d\uddef\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:spades:`. Represented in Unicode as ♠️. */
    object Spades : UnicodeEmoji("\u2660\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:clubs:`. Represented in Unicode as ♣️. */
    object Clubs : UnicodeEmoji("\u2663\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:hearts:`. Represented in Unicode as ♥️. */
    object Hearts : UnicodeEmoji("\u2665\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:diamonds:`. Represented in Unicode as ♦️. */
    object Diamonds : UnicodeEmoji("\u2666\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:black_joker:`. Represented in Unicode as 🃏. */
    object BlackJoker : UnicodeEmoji("\ud83c\udccf")

    /** Unicode emoji with a Discord shortcode of `:flower_playing_cards:`. Represented in Unicode as 🎴. */
    object FlowerPlayingCards : UnicodeEmoji("\ud83c\udfb4")

    /** Unicode emoji with a Discord shortcode of `:mahjong:`. Represented in Unicode as 🀄. */
    object Mahjong : UnicodeEmoji("\ud83c\udc04")

    /** Unicode emoji with a Discord shortcode of `:clock1:`. Represented in Unicode as 🕐. */
    object Clock1 : UnicodeEmoji("\ud83d\udd50")

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

    /** Unicode emoji with a Discord shortcode of `:female_sign:`. Represented in Unicode as ♀️. */
    object FemaleSign : UnicodeEmoji("\u2640\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:male_sign:`. Represented in Unicode as ♂️. */
    object MaleSign : UnicodeEmoji("\u2642\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:medical_symbol:`. Represented in Unicode as ⚕️. */
    object MedicalSymbol : UnicodeEmoji("\u2695\ufe0f")

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

    /** Unicode emoji with a Discord shortcode of `:flag_white:`. Represented in Unicode as 🏳️. */
    object FlagWhitE : UnicodeEmoji("\ud83c\udff3\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:flag_black:`. Represented in Unicode as 🏴. */
    object FlagBlacK : UnicodeEmoji("\ud83c\udff4")

    /** Unicode emoji with a Discord shortcode of `:checkered_flag:`. Represented in Unicode as 🏁. */
    object CheckeredFlag : UnicodeEmoji("\ud83c\udfc1")

    /** Unicode emoji with a Discord shortcode of `:triangular_flag_on_post:`. Represented in Unicode as 🚩. */
    object TriangularFlagOnPost : UnicodeEmoji("\ud83d\udea9")

    /** Unicode emoji with Discord shortcodes of `:rainbow_flag:` and `:gay_pride_flag:`. Represented in Unicode as 🏳️‍🌈. */
    object RainbowFlag : UnicodeEmoji("\ud83c\udff3\ufe0f\u200d\ud83c\udf08")

    /** Unicode emoji with a Discord shortcode of `:pirate_flag:`. Represented in Unicode as 🏴‍☠️. */
    object PirateFlag : UnicodeEmoji("\ud83c\udff4\u200d\u2620\ufe0f")

    /** Unicode emoji with a Discord shortcode of `:flag_af:`. Represented in Unicode as 🇦🇫. */
    object FlagAF : UnicodeEmoji("\ud83c\udde6\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_ax:`. Represented in Unicode as 🇦🇽. */
    object FlagAX : UnicodeEmoji("\ud83c\udde6\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_al:`. Represented in Unicode as 🇦🇱. */
    object FlagAL : UnicodeEmoji("\ud83c\udde6\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_dz:`. Represented in Unicode as 🇩🇿. */
    object FlagDZ : UnicodeEmoji("\ud83c\udde9\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_as:`. Represented in Unicode as 🇦🇸. */
    object FlagAS : UnicodeEmoji("\ud83c\udde6\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_ad:`. Represented in Unicode as 🇦🇩. */
    object FlagAD : UnicodeEmoji("\ud83c\udde6\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ao:`. Represented in Unicode as 🇦🇴. */
    object FlagAO : UnicodeEmoji("\ud83c\udde6\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_ai:`. Represented in Unicode as 🇦🇮. */
    object FlagAI : UnicodeEmoji("\ud83c\udde6\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_aq:`. Represented in Unicode as 🇦🇶. */
    object FlagAQ : UnicodeEmoji("\ud83c\udde6\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_ag:`. Represented in Unicode as 🇦🇬. */
    object FlagAG : UnicodeEmoji("\ud83c\udde6\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ar:`. Represented in Unicode as 🇦🇷. */
    object FlagAR : UnicodeEmoji("\ud83c\udde6\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_am:`. Represented in Unicode as 🇦🇲. */
    object FlagAM : UnicodeEmoji("\ud83c\udde6\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_aw:`. Represented in Unicode as 🇦🇼. */
    object FlagAW : UnicodeEmoji("\ud83c\udde6\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_au:`. Represented in Unicode as 🇦🇺. */
    object FlagAU : UnicodeEmoji("\ud83c\udde6\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_at:`. Represented in Unicode as 🇦🇹. */
    object FlagAT : UnicodeEmoji("\ud83c\udde6\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_az:`. Represented in Unicode as 🇦🇿. */
    object FlagAZ : UnicodeEmoji("\ud83c\udde6\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_bs:`. Represented in Unicode as 🇧🇸. */
    object FlagBS : UnicodeEmoji("\ud83c\udde7\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_bh:`. Represented in Unicode as 🇧🇭. */
    object FlagBH : UnicodeEmoji("\ud83c\udde7\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_bd:`. Represented in Unicode as 🇧🇩. */
    object FlagBD : UnicodeEmoji("\ud83c\udde7\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_bb:`. Represented in Unicode as 🇧🇧. */
    object FlagBB : UnicodeEmoji("\ud83c\udde7\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_by:`. Represented in Unicode as 🇧🇾. */
    object FlagBY : UnicodeEmoji("\ud83c\udde7\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_be:`. Represented in Unicode as 🇧🇪. */
    object FlagBE : UnicodeEmoji("\ud83c\udde7\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_bz:`. Represented in Unicode as 🇧🇿. */
    object FlagBZ : UnicodeEmoji("\ud83c\udde7\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_bj:`. Represented in Unicode as 🇧🇯. */
    object FlagBJ : UnicodeEmoji("\ud83c\udde7\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_bm:`. Represented in Unicode as 🇧🇲. */
    object FlagBM : UnicodeEmoji("\ud83c\udde7\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_bt:`. Represented in Unicode as 🇧🇹. */
    object FlagBT : UnicodeEmoji("\ud83c\udde7\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_bo:`. Represented in Unicode as 🇧🇴. */
    object FlagBO : UnicodeEmoji("\ud83c\udde7\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_ba:`. Represented in Unicode as 🇧🇦. */
    object FlagBA : UnicodeEmoji("\ud83c\udde7\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_bw:`. Represented in Unicode as 🇧🇼. */
    object FlagBW : UnicodeEmoji("\ud83c\udde7\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_br:`. Represented in Unicode as 🇧🇷. */
    object FlagBR : UnicodeEmoji("\ud83c\udde7\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_io:`. Represented in Unicode as 🇮🇴. */
    object FlagIO : UnicodeEmoji("\ud83c\uddee\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_vg:`. Represented in Unicode as 🇻🇬. */
    object FlagVG : UnicodeEmoji("\ud83c\uddfb\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_bn:`. Represented in Unicode as 🇧🇳. */
    object FlagBN : UnicodeEmoji("\ud83c\udde7\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_bg:`. Represented in Unicode as 🇧🇬. */
    object FlagBG : UnicodeEmoji("\ud83c\udde7\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_bf:`. Represented in Unicode as 🇧🇫. */
    object FlagBF : UnicodeEmoji("\ud83c\udde7\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_bi:`. Represented in Unicode as 🇧🇮. */
    object FlagBI : UnicodeEmoji("\ud83c\udde7\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_kh:`. Represented in Unicode as 🇰🇭. */
    object FlagKH : UnicodeEmoji("\ud83c\uddf0\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_cm:`. Represented in Unicode as 🇨🇲. */
    object FlagCM : UnicodeEmoji("\ud83c\udde8\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_ca:`. Represented in Unicode as 🇨🇦. */
    object FlagCA : UnicodeEmoji("\ud83c\udde8\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_ic:`. Represented in Unicode as 🇮🇨. */
    object FlagIC : UnicodeEmoji("\ud83c\uddee\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_cv:`. Represented in Unicode as 🇨🇻. */
    object FlagCV : UnicodeEmoji("\ud83c\udde8\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_bq:`. Represented in Unicode as 🇧🇶. */
    object FlagBQ : UnicodeEmoji("\ud83c\udde7\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_ky:`. Represented in Unicode as 🇰🇾. */
    object FlagKY : UnicodeEmoji("\ud83c\uddf0\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_cf:`. Represented in Unicode as 🇨🇫. */
    object FlagCF : UnicodeEmoji("\ud83c\udde8\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_td:`. Represented in Unicode as 🇹🇩. */
    object FlagTD : UnicodeEmoji("\ud83c\uddf9\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_cl:`. Represented in Unicode as 🇨🇱. */
    object FlagCL : UnicodeEmoji("\ud83c\udde8\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_cn:`. Represented in Unicode as 🇨🇳. */
    object FlagCN : UnicodeEmoji("\ud83c\udde8\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_cx:`. Represented in Unicode as 🇨🇽. */
    object FlagCX : UnicodeEmoji("\ud83c\udde8\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_cc:`. Represented in Unicode as 🇨🇨. */
    object FlagCC : UnicodeEmoji("\ud83c\udde8\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_co:`. Represented in Unicode as 🇨🇴. */
    object FlagCO : UnicodeEmoji("\ud83c\udde8\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_km:`. Represented in Unicode as 🇰🇲. */
    object FlagKM : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_cg:`. Represented in Unicode as 🇨🇬. */
    object FlagCG : UnicodeEmoji("\ud83c\udde8\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_cd:`. Represented in Unicode as 🇨🇩. */
    object FlagCD : UnicodeEmoji("\ud83c\udde8\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ck:`. Represented in Unicode as 🇨🇰. */
    object FlagCK : UnicodeEmoji("\ud83c\udde8\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_cr:`. Represented in Unicode as 🇨🇷. */
    object FlagCR : UnicodeEmoji("\ud83c\udde8\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ci:`. Represented in Unicode as 🇨🇮. */
    object FlagCI : UnicodeEmoji("\ud83c\udde8\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_hr:`. Represented in Unicode as 🇭🇷. */
    object FlagHR : UnicodeEmoji("\ud83c\udded\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_cu:`. Represented in Unicode as 🇨🇺. */
    object FlagCU : UnicodeEmoji("\ud83c\udde8\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_cw:`. Represented in Unicode as 🇨🇼. */
    object FlagCW : UnicodeEmoji("\ud83c\udde8\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_cy:`. Represented in Unicode as 🇨🇾. */
    object FlagCY : UnicodeEmoji("\ud83c\udde8\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_cz:`. Represented in Unicode as 🇨🇿. */
    object FlagCZ : UnicodeEmoji("\ud83c\udde8\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_dk:`. Represented in Unicode as 🇩🇰. */
    object FlagDK : UnicodeEmoji("\ud83c\udde9\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_dj:`. Represented in Unicode as 🇩🇯. */
    object FlagDJ : UnicodeEmoji("\ud83c\udde9\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_dm:`. Represented in Unicode as 🇩🇲. */
    object FlagDM : UnicodeEmoji("\ud83c\udde9\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_do:`. Represented in Unicode as 🇩🇴. */
    object FlagDO : UnicodeEmoji("\ud83c\udde9\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_ec:`. Represented in Unicode as 🇪🇨. */
    object FlagEC : UnicodeEmoji("\ud83c\uddea\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_eg:`. Represented in Unicode as 🇪🇬. */
    object FlagEG : UnicodeEmoji("\ud83c\uddea\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_sv:`. Represented in Unicode as 🇸🇻. */
    object FlagSV : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_gq:`. Represented in Unicode as 🇬🇶. */
    object FlagGQ : UnicodeEmoji("\ud83c\uddec\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_er:`. Represented in Unicode as 🇪🇷. */
    object FlagER : UnicodeEmoji("\ud83c\uddea\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ee:`. Represented in Unicode as 🇪🇪. */
    object FlagEE : UnicodeEmoji("\ud83c\uddea\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_et:`. Represented in Unicode as 🇪🇹. */
    object FlagET : UnicodeEmoji("\ud83c\uddea\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_eu:`. Represented in Unicode as 🇪🇺. */
    object FlagEU : UnicodeEmoji("\ud83c\uddea\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_fk:`. Represented in Unicode as 🇫🇰. */
    object FlagFK : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_fo:`. Represented in Unicode as 🇫🇴. */
    object FlagFO : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_fj:`. Represented in Unicode as 🇫🇯. */
    object FlagFJ : UnicodeEmoji("\ud83c\uddeb\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_fi:`. Represented in Unicode as 🇫🇮. */
    object FlagFI : UnicodeEmoji("\ud83c\uddeb\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_fr:`. Represented in Unicode as 🇫🇷. */
    object FlagFR : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_gf:`. Represented in Unicode as 🇬🇫. */
    object FlagGF : UnicodeEmoji("\ud83c\uddec\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_pf:`. Represented in Unicode as 🇵🇫. */
    object FlagPF : UnicodeEmoji("\ud83c\uddf5\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_tf:`. Represented in Unicode as 🇹🇫. */
    object FlagTF : UnicodeEmoji("\ud83c\uddf9\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_ga:`. Represented in Unicode as 🇬🇦. */
    object FlagGA : UnicodeEmoji("\ud83c\uddec\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_gm:`. Represented in Unicode as 🇬🇲. */
    object FlagGM : UnicodeEmoji("\ud83c\uddec\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_ge:`. Represented in Unicode as 🇬🇪. */
    object FlagGE : UnicodeEmoji("\ud83c\uddec\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_de:`. Represented in Unicode as 🇩🇪. */
    object FlagDE : UnicodeEmoji("\ud83c\udde9\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_gh:`. Represented in Unicode as 🇬🇭. */
    object FlagGH : UnicodeEmoji("\ud83c\uddec\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_gi:`. Represented in Unicode as 🇬🇮. */
    object FlagGI : UnicodeEmoji("\ud83c\uddec\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_gr:`. Represented in Unicode as 🇬🇷. */
    object FlagGR : UnicodeEmoji("\ud83c\uddec\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_gl:`. Represented in Unicode as 🇬🇱. */
    object FlagGL : UnicodeEmoji("\ud83c\uddec\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_gd:`. Represented in Unicode as 🇬🇩. */
    object FlagGD : UnicodeEmoji("\ud83c\uddec\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_gp:`. Represented in Unicode as 🇬🇵. */
    object FlagGP : UnicodeEmoji("\ud83c\uddec\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_gu:`. Represented in Unicode as 🇬🇺. */
    object FlagGU : UnicodeEmoji("\ud83c\uddec\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_gt:`. Represented in Unicode as 🇬🇹. */
    object FlagGT : UnicodeEmoji("\ud83c\uddec\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_gg:`. Represented in Unicode as 🇬🇬. */
    object FlagGG : UnicodeEmoji("\ud83c\uddec\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_gn:`. Represented in Unicode as 🇬🇳. */
    object FlagGN : UnicodeEmoji("\ud83c\uddec\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_gw:`. Represented in Unicode as 🇬🇼. */
    object FlagGW : UnicodeEmoji("\ud83c\uddec\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_gy:`. Represented in Unicode as 🇬🇾. */
    object FlagGY : UnicodeEmoji("\ud83c\uddec\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_ht:`. Represented in Unicode as 🇭🇹. */
    object FlagHT : UnicodeEmoji("\ud83c\udded\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_hn:`. Represented in Unicode as 🇭🇳. */
    object FlagHN : UnicodeEmoji("\ud83c\udded\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_hk:`. Represented in Unicode as 🇭🇰. */
    object FlagHK : UnicodeEmoji("\ud83c\udded\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_hu:`. Represented in Unicode as 🇭🇺. */
    object FlagHU : UnicodeEmoji("\ud83c\udded\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_is:`. Represented in Unicode as 🇮🇸. */
    object FlagIS : UnicodeEmoji("\ud83c\uddee\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_in:`. Represented in Unicode as 🇮🇳. */
    object FlagIN : UnicodeEmoji("\ud83c\uddee\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_id:`. Represented in Unicode as 🇮🇩. */
    object FlagID : UnicodeEmoji("\ud83c\uddee\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_ir:`. Represented in Unicode as 🇮🇷. */
    object FlagIR : UnicodeEmoji("\ud83c\uddee\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_iq:`. Represented in Unicode as 🇮🇶. */
    object FlagIQ : UnicodeEmoji("\ud83c\uddee\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_ie:`. Represented in Unicode as 🇮🇪. */
    object FlagIE : UnicodeEmoji("\ud83c\uddee\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_im:`. Represented in Unicode as 🇮🇲. */
    object FlagIM : UnicodeEmoji("\ud83c\uddee\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_il:`. Represented in Unicode as 🇮🇱. */
    object FlagIL : UnicodeEmoji("\ud83c\uddee\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_it:`. Represented in Unicode as 🇮🇹. */
    object FlagIT : UnicodeEmoji("\ud83c\uddee\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_jm:`. Represented in Unicode as 🇯🇲. */
    object FlagJM : UnicodeEmoji("\ud83c\uddef\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_jp:`. Represented in Unicode as 🇯🇵. */
    object FlagJP : UnicodeEmoji("\ud83c\uddef\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:crossed_flags:`. Represented in Unicode as 🎌. */
    object CrossedFlags : UnicodeEmoji("\ud83c\udf8c")

    /** Unicode emoji with a Discord shortcode of `:flag_je:`. Represented in Unicode as 🇯🇪. */
    object FlagJE : UnicodeEmoji("\ud83c\uddef\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_jo:`. Represented in Unicode as 🇯🇴. */
    object FlagJO : UnicodeEmoji("\ud83c\uddef\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_kz:`. Represented in Unicode as 🇰🇿. */
    object FlagKZ : UnicodeEmoji("\ud83c\uddf0\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ke:`. Represented in Unicode as 🇰🇪. */
    object FlagKE : UnicodeEmoji("\ud83c\uddf0\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ki:`. Represented in Unicode as 🇰🇮. */
    object FlagKI : UnicodeEmoji("\ud83c\uddf0\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_xk:`. Represented in Unicode as 🇽🇰. */
    object FlagXK : UnicodeEmoji("\ud83c\uddfd\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_kw:`. Represented in Unicode as 🇰🇼. */
    object FlagKW : UnicodeEmoji("\ud83c\uddf0\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_kg:`. Represented in Unicode as 🇰🇬. */
    object FlagKG : UnicodeEmoji("\ud83c\uddf0\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_la:`. Represented in Unicode as 🇱🇦. */
    object FlagLA : UnicodeEmoji("\ud83c\uddf1\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_lv:`. Represented in Unicode as 🇱🇻. */
    object FlagLV : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_lb:`. Represented in Unicode as 🇱🇧. */
    object FlagLB : UnicodeEmoji("\ud83c\uddf1\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_ls:`. Represented in Unicode as 🇱🇸. */
    object FlagLS : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_lr:`. Represented in Unicode as 🇱🇷. */
    object FlagLR : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ly:`. Represented in Unicode as 🇱🇾. */
    object FlagLY : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_li:`. Represented in Unicode as 🇱🇮. */
    object FlagLI : UnicodeEmoji("\ud83c\uddf1\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_lt:`. Represented in Unicode as 🇱🇹. */
    object FlagLT : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_lu:`. Represented in Unicode as 🇱🇺. */
    object FlagLU : UnicodeEmoji("\ud83c\uddf1\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_mo:`. Represented in Unicode as 🇲🇴. */
    object FlagMO : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_mk:`. Represented in Unicode as 🇲🇰. */
    object FlagMK : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_mg:`. Represented in Unicode as 🇲🇬. */
    object FlagMG : UnicodeEmoji("\ud83c\uddf2\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_mw:`. Represented in Unicode as 🇲🇼. */
    object FlagMW : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_my:`. Represented in Unicode as 🇲🇾. */
    object FlagMY : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_mv:`. Represented in Unicode as 🇲🇻. */
    object FlagMV : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_ml:`. Represented in Unicode as 🇲🇱. */
    object FlagML : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_mt:`. Represented in Unicode as 🇲🇹. */
    object FlagMT : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_mh:`. Represented in Unicode as 🇲🇭. */
    object FlagMH : UnicodeEmoji("\ud83c\uddf2\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_mq:`. Represented in Unicode as 🇲🇶. */
    object FlagMQ : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf6")

    /** Unicode emoji with a Discord shortcode of `:flag_mr:`. Represented in Unicode as 🇲🇷. */
    object FlagMR : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_mu:`. Represented in Unicode as 🇲🇺. */
    object FlagMU : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_yt:`. Represented in Unicode as 🇾🇹. */
    object FlagYT : UnicodeEmoji("\ud83c\uddfe\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_mx:`. Represented in Unicode as 🇲🇽. */
    object FlagMX : UnicodeEmoji("\ud83c\uddf2\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_fm:`. Represented in Unicode as 🇫🇲. */
    object FlagFM : UnicodeEmoji("\ud83c\uddeb\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_md:`. Represented in Unicode as 🇲🇩. */
    object FlagMD : UnicodeEmoji("\ud83c\uddf2\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_mc:`. Represented in Unicode as 🇲🇨. */
    object FlagMC : UnicodeEmoji("\ud83c\uddf2\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_mn:`. Represented in Unicode as 🇲🇳. */
    object FlagMN : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_me:`. Represented in Unicode as 🇲🇪. */
    object FlagME : UnicodeEmoji("\ud83c\uddf2\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ms:`. Represented in Unicode as 🇲🇸. */
    object FlagMS : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_ma:`. Represented in Unicode as 🇲🇦. */
    object FlagMA : UnicodeEmoji("\ud83c\uddf2\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_mz:`. Represented in Unicode as 🇲🇿. */
    object FlagMZ : UnicodeEmoji("\ud83c\uddf2\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_mm:`. Represented in Unicode as 🇲🇲. */
    object FlagMM : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_na:`. Represented in Unicode as 🇳🇦. */
    object FlagNA : UnicodeEmoji("\ud83c\uddf3\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_nr:`. Represented in Unicode as 🇳🇷. */
    object FlagNR : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_np:`. Represented in Unicode as 🇳🇵. */
    object FlagNP : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_nl:`. Represented in Unicode as 🇳🇱. */
    object FlagNL : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_nc:`. Represented in Unicode as 🇳🇨. */
    object FlagNC : UnicodeEmoji("\ud83c\uddf3\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_nz:`. Represented in Unicode as 🇳🇿. */
    object FlagNZ : UnicodeEmoji("\ud83c\uddf3\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_ni:`. Represented in Unicode as 🇳🇮. */
    object FlagNI : UnicodeEmoji("\ud83c\uddf3\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_ne:`. Represented in Unicode as 🇳🇪. */
    object FlagNE : UnicodeEmoji("\ud83c\uddf3\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ng:`. Represented in Unicode as 🇳🇬. */
    object FlagNG : UnicodeEmoji("\ud83c\uddf3\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_nu:`. Represented in Unicode as 🇳🇺. */
    object FlagNU : UnicodeEmoji("\ud83c\uddf3\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_nf:`. Represented in Unicode as 🇳🇫. */
    object FlagNF : UnicodeEmoji("\ud83c\uddf3\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_kp:`. Represented in Unicode as 🇰🇵. */
    object FlagKP : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_mp:`. Represented in Unicode as 🇲🇵. */
    object FlagMP : UnicodeEmoji("\ud83c\uddf2\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_no:`. Represented in Unicode as 🇳🇴. */
    object FlagNO : UnicodeEmoji("\ud83c\uddf3\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_om:`. Represented in Unicode as 🇴🇲. */
    object FlagOM : UnicodeEmoji("\ud83c\uddf4\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_pk:`. Represented in Unicode as 🇵🇰. */
    object FlagPK : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_pw:`. Represented in Unicode as 🇵🇼. */
    object FlagPW : UnicodeEmoji("\ud83c\uddf5\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_ps:`. Represented in Unicode as 🇵🇸. */
    object FlagPS : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_pa:`. Represented in Unicode as 🇵🇦. */
    object FlagPA : UnicodeEmoji("\ud83c\uddf5\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_pg:`. Represented in Unicode as 🇵🇬. */
    object FlagPG : UnicodeEmoji("\ud83c\uddf5\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_py:`. Represented in Unicode as 🇵🇾. */
    object FlagPY : UnicodeEmoji("\ud83c\uddf5\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_pe:`. Represented in Unicode as 🇵🇪. */
    object FlagPE : UnicodeEmoji("\ud83c\uddf5\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ph:`. Represented in Unicode as 🇵🇭. */
    object FlagPH : UnicodeEmoji("\ud83c\uddf5\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_pn:`. Represented in Unicode as 🇵🇳. */
    object FlagPN : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_pl:`. Represented in Unicode as 🇵🇱. */
    object FlagPL : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_pt:`. Represented in Unicode as 🇵🇹. */
    object FlagPT : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_pr:`. Represented in Unicode as 🇵🇷. */
    object FlagPR : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_qa:`. Represented in Unicode as 🇶🇦. */
    object FlagQA : UnicodeEmoji("\ud83c\uddf6\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_re:`. Represented in Unicode as 🇷🇪. */
    object FlagRE : UnicodeEmoji("\ud83c\uddf7\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ro:`. Represented in Unicode as 🇷🇴. */
    object FlagRO : UnicodeEmoji("\ud83c\uddf7\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_ru:`. Represented in Unicode as 🇷🇺. */
    object FlagRU : UnicodeEmoji("\ud83c\uddf7\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_rw:`. Represented in Unicode as 🇷🇼. */
    object FlagRW : UnicodeEmoji("\ud83c\uddf7\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_ws:`. Represented in Unicode as 🇼🇸. */
    object FlagWS : UnicodeEmoji("\ud83c\uddfc\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_sm:`. Represented in Unicode as 🇸🇲. */
    object FlagSM : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_st:`. Represented in Unicode as 🇸🇹. */
    object FlagST : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_sa:`. Represented in Unicode as 🇸🇦. */
    object FlagSA : UnicodeEmoji("\ud83c\uddf8\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_sn:`. Represented in Unicode as 🇸🇳. */
    object FlagSN : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_rs:`. Represented in Unicode as 🇷🇸. */
    object FlagRS : UnicodeEmoji("\ud83c\uddf7\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_sc:`. Represented in Unicode as 🇸🇨. */
    object FlagSC : UnicodeEmoji("\ud83c\uddf8\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_sl:`. Represented in Unicode as 🇸🇱. */
    object FlagSL : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_sg:`. Represented in Unicode as 🇸🇬. */
    object FlagSG : UnicodeEmoji("\ud83c\uddf8\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_sx:`. Represented in Unicode as 🇸🇽. */
    object FlagSX : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfd")

    /** Unicode emoji with a Discord shortcode of `:flag_sk:`. Represented in Unicode as 🇸🇰. */
    object FlagSK : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_si:`. Represented in Unicode as 🇸🇮. */
    object FlagSI : UnicodeEmoji("\ud83c\uddf8\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_gs:`. Represented in Unicode as 🇬🇸. */
    object FlagGS : UnicodeEmoji("\ud83c\uddec\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_sb:`. Represented in Unicode as 🇸🇧. */
    object FlagSB : UnicodeEmoji("\ud83c\uddf8\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:flag_so:`. Represented in Unicode as 🇸🇴. */
    object FlagSO : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_za:`. Represented in Unicode as 🇿🇦. */
    object FlagZA : UnicodeEmoji("\ud83c\uddff\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_kr:`. Represented in Unicode as 🇰🇷. */
    object FlagKR : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_ss:`. Represented in Unicode as 🇸🇸. */
    object FlagSS : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_es:`. Represented in Unicode as 🇪🇸. */
    object FlagES : UnicodeEmoji("\ud83c\uddea\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_lk:`. Represented in Unicode as 🇱🇰. */
    object FlagLK : UnicodeEmoji("\ud83c\uddf1\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_bl:`. Represented in Unicode as 🇧🇱. */
    object FlagBL : UnicodeEmoji("\ud83c\udde7\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_sh:`. Represented in Unicode as 🇸🇭. */
    object FlagSH : UnicodeEmoji("\ud83c\uddf8\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_kn:`. Represented in Unicode as 🇰🇳. */
    object FlagKN : UnicodeEmoji("\ud83c\uddf0\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_lc:`. Represented in Unicode as 🇱🇨. */
    object FlagLC : UnicodeEmoji("\ud83c\uddf1\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_pm:`. Represented in Unicode as 🇵🇲. */
    object FlagPM : UnicodeEmoji("\ud83c\uddf5\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_vc:`. Represented in Unicode as 🇻🇨. */
    object FlagVC : UnicodeEmoji("\ud83c\uddfb\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_sd:`. Represented in Unicode as 🇸🇩. */
    object FlagSD : UnicodeEmoji("\ud83c\uddf8\ud83c\udde9")

    /** Unicode emoji with a Discord shortcode of `:flag_sr:`. Represented in Unicode as 🇸🇷. */
    object FlagSR : UnicodeEmoji("\ud83c\uddf8\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_sz:`. Represented in Unicode as 🇸🇿. */
    object FlagSZ : UnicodeEmoji("\ud83c\uddf8\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_se:`. Represented in Unicode as 🇸🇪. */
    object FlagSE : UnicodeEmoji("\ud83c\uddf8\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_ch:`. Represented in Unicode as 🇨🇭. */
    object FlagCH : UnicodeEmoji("\ud83c\udde8\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_sy:`. Represented in Unicode as 🇸🇾. */
    object FlagSY : UnicodeEmoji("\ud83c\uddf8\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_tw:`. Represented in Unicode as 🇹🇼. */
    object FlagTW : UnicodeEmoji("\ud83c\uddf9\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_tj:`. Represented in Unicode as 🇹🇯. */
    object FlagTJ : UnicodeEmoji("\ud83c\uddf9\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_tz:`. Represented in Unicode as 🇹🇿. */
    object FlagTZ : UnicodeEmoji("\ud83c\uddf9\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_th:`. Represented in Unicode as 🇹🇭. */
    object FlagTH : UnicodeEmoji("\ud83c\uddf9\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_tl:`. Represented in Unicode as 🇹🇱. */
    object FlagTL : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf1")

    /** Unicode emoji with a Discord shortcode of `:flag_tg:`. Represented in Unicode as 🇹🇬. */
    object FlagTG : UnicodeEmoji("\ud83c\uddf9\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_tk:`. Represented in Unicode as 🇹🇰. */
    object FlagTK : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf0")

    /** Unicode emoji with a Discord shortcode of `:flag_to:`. Represented in Unicode as 🇹🇴. */
    object FlagTO : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf4")

    /** Unicode emoji with a Discord shortcode of `:flag_tt:`. Represented in Unicode as 🇹🇹. */
    object FlagTT : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf9")

    /** Unicode emoji with a Discord shortcode of `:flag_tn:`. Represented in Unicode as 🇹🇳. */
    object FlagTN : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_tr:`. Represented in Unicode as 🇹🇷. */
    object FlagTR : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf7")

    /** Unicode emoji with a Discord shortcode of `:flag_tm:`. Represented in Unicode as 🇹🇲. */
    object FlagTM : UnicodeEmoji("\ud83c\uddf9\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_tc:`. Represented in Unicode as 🇹🇨. */
    object FlagTC : UnicodeEmoji("\ud83c\uddf9\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_vi:`. Represented in Unicode as 🇻🇮. */
    object FlagVI : UnicodeEmoji("\ud83c\uddfb\ud83c\uddee")

    /** Unicode emoji with a Discord shortcode of `:flag_tv:`. Represented in Unicode as 🇹🇻. */
    object FlagTV : UnicodeEmoji("\ud83c\uddf9\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_ug:`. Represented in Unicode as 🇺🇬. */
    object FlagUG : UnicodeEmoji("\ud83c\uddfa\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_ua:`. Represented in Unicode as 🇺🇦. */
    object FlagUA : UnicodeEmoji("\ud83c\uddfa\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_ae:`. Represented in Unicode as 🇦🇪. */
    object FlagAE : UnicodeEmoji("\ud83c\udde6\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_gb:`. Represented in Unicode as 🇬🇧. */
    object FlagGB : UnicodeEmoji("\ud83c\uddec\ud83c\udde7")

    /** Unicode emoji with a Discord shortcode of `:england:`. Represented in Unicode as 🏴󠁧󠁢󠁥󠁮󠁧󠁿. */
    object England : UnicodeEmoji("\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc65\udb40\udc6e\udb40\udc67\udb40\udc7f")

    /** Unicode emoji with a Discord shortcode of `:scotland:`. Represented in Unicode as 🏴󠁧󠁢󠁳󠁣󠁴󠁿. */
    object Scotland : UnicodeEmoji("\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc73\udb40\udc63\udb40\udc74\udb40\udc7f")

    /** Unicode emoji with a Discord shortcode of `:wales:`. Represented in Unicode as 🏴󠁧󠁢󠁷󠁬󠁳󠁿. */
    object Wales : UnicodeEmoji("\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc77\udb40\udc6c\udb40\udc73\udb40\udc7f")

    /** Unicode emoji with a Discord shortcode of `:flag_us:`. Represented in Unicode as 🇺🇸. */
    object FlagUS : UnicodeEmoji("\ud83c\uddfa\ud83c\uddf8")

    /** Unicode emoji with a Discord shortcode of `:flag_uy:`. Represented in Unicode as 🇺🇾. */
    object FlagUY : UnicodeEmoji("\ud83c\uddfa\ud83c\uddfe")

    /** Unicode emoji with a Discord shortcode of `:flag_uz:`. Represented in Unicode as 🇺🇿. */
    object FlagUZ : UnicodeEmoji("\ud83c\uddfa\ud83c\uddff")

    /** Unicode emoji with a Discord shortcode of `:flag_vu:`. Represented in Unicode as 🇻🇺. */
    object FlagVU : UnicodeEmoji("\ud83c\uddfb\ud83c\uddfa")

    /** Unicode emoji with a Discord shortcode of `:flag_va:`. Represented in Unicode as 🇻🇦. */
    object FlagVA : UnicodeEmoji("\ud83c\uddfb\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_ve:`. Represented in Unicode as 🇻🇪. */
    object FlagVE : UnicodeEmoji("\ud83c\uddfb\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_vn:`. Represented in Unicode as 🇻🇳. */
    object FlagVN : UnicodeEmoji("\ud83c\uddfb\ud83c\uddf3")

    /** Unicode emoji with a Discord shortcode of `:flag_wf:`. Represented in Unicode as 🇼🇫. */
    object FlagWF : UnicodeEmoji("\ud83c\uddfc\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_eh:`. Represented in Unicode as 🇪🇭. */
    object FlagEH : UnicodeEmoji("\ud83c\uddea\ud83c\udded")

    /** Unicode emoji with a Discord shortcode of `:flag_ye:`. Represented in Unicode as 🇾🇪. */
    object FlagYE : UnicodeEmoji("\ud83c\uddfe\ud83c\uddea")

    /** Unicode emoji with a Discord shortcode of `:flag_zm:`. Represented in Unicode as 🇿🇲. */
    object FlagZM : UnicodeEmoji("\ud83c\uddff\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_zw:`. Represented in Unicode as 🇿🇼. */
    object FlagZW : UnicodeEmoji("\ud83c\uddff\ud83c\uddfc")

    /** Unicode emoji with a Discord shortcode of `:flag_ac:`. Represented in Unicode as 🇦🇨. */
    object FlagAC : UnicodeEmoji("\ud83c\udde6\ud83c\udde8")

    /** Unicode emoji with a Discord shortcode of `:flag_bv:`. Represented in Unicode as 🇧🇻. */
    object FlagBV : UnicodeEmoji("\ud83c\udde7\ud83c\uddfb")

    /** Unicode emoji with a Discord shortcode of `:flag_cp:`. Represented in Unicode as 🇨🇵. */
    object FlagCP : UnicodeEmoji("\ud83c\udde8\ud83c\uddf5")

    /** Unicode emoji with a Discord shortcode of `:flag_ea:`. Represented in Unicode as 🇪🇦. */
    object FlagEA : UnicodeEmoji("\ud83c\uddea\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_dg:`. Represented in Unicode as 🇩🇬. */
    object FlagDG : UnicodeEmoji("\ud83c\udde9\ud83c\uddec")

    /** Unicode emoji with a Discord shortcode of `:flag_hm:`. Represented in Unicode as 🇭🇲. */
    object FlagHM : UnicodeEmoji("\ud83c\udded\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:flag_mf:`. Represented in Unicode as 🇲🇫. */
    object FlagMF : UnicodeEmoji("\ud83c\uddf2\ud83c\uddeb")

    /** Unicode emoji with a Discord shortcode of `:flag_sj:`. Represented in Unicode as 🇸🇯. */
    object FlagSJ : UnicodeEmoji("\ud83c\uddf8\ud83c\uddef")

    /** Unicode emoji with a Discord shortcode of `:flag_ta:`. Represented in Unicode as 🇹🇦. */
    object FlagTA : UnicodeEmoji("\ud83c\uddf9\ud83c\udde6")

    /** Unicode emoji with a Discord shortcode of `:flag_um:`. Represented in Unicode as 🇺🇲. */
    object FlagUM : UnicodeEmoji("\ud83c\uddfa\ud83c\uddf2")

    /** Unicode emoji with a Discord shortcode of `:united_nations:`. Represented in Unicode as 🇺🇳. */
    object UnitedNations : UnicodeEmoji("\ud83c\uddfa\ud83c\uddf3")

    /** Get this [UnicodeEmoji] as [String] (aka [combinedUnicode]). */
    override fun toString(): String = combinedUnicode

    /** Check if [other] is the same as this [UnicodeEmoji]. */
    override fun equals(other: Any?): Boolean = other.toString() == toString()

    companion object {
        /** A list of 0-10 emojis. */
        val numbers: List<UnicodeEmoji> = listOf(Zero, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, KeycapTen)

        /** Returns given [Int] as a [List] of [UnicodeEmoji]s */
        fun fromInt(num: Int): List<UnicodeEmoji> = num.toString().map { numbers[it.toString().toInt()] }
    }
}

/** Returns [List] of [UnicodeEmoji]s as [String] */
val List<UnicodeEmoji>.asString get() = joinToString("") { it.toString() }

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
    id?.let { ForeignGuildEmoji(context, id, name) } ?: UnicodeEmoji.fromUnicode(name)
