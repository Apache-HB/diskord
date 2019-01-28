
import com.serebit.strife.data.Permission
import com.serebit.strife.data.toPermissions
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionTests {
    private val permissionTestSets = listOf(
        2228240.toPermissions() to
                setOf(Permission.General.ManageChannels, Permission.Text.MentionEveryone, Permission.Voice.Speak),
        114688.toPermissions() to
                setOf(Permission.Text.EmbedLinks, Permission.Text.AttachFiles, Permission.Text.ReadMessageHistory),
        1073741954.toPermissions() to
                setOf(Permission.General.ViewAuditLog, Permission.General.KickMembers, Permission.General.ManageEmotes)
    )

    @Test
    fun `bit set should translate to correct permissions`() {
        permissionTestSets.forEach { assertEquals(it.first, it.second) }
    }
}
