import com.serebit.strife.data.Permission
import com.serebit.strife.data.toPermissions
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionTests {
    private val permissionTestSets = listOf(
        2228240.toPermissions() to setOf(Permission.ManageChannels, Permission.MentionEveryone, Permission.Speak),
        114688.toPermissions() to setOf(Permission.EmbedLinks, Permission.AttachFiles, Permission.ReadMessageHistory),
        1073741954.toPermissions() to setOf(Permission.ViewAuditLog, Permission.KickMembers, Permission.ManageEmotes)
    )

    @Test
    fun `bit set should translate to correct permissions`() {
        permissionTestSets.forEach { assertEquals(it.first, it.second) }
    }
}
