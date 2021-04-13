package no.elhub.tools.autorelease

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.util.FS

/**
 * This handles setting up authentication for the git client
 */
class SshConfig(val sshKeyPath: String, val sshPassPhrase: String?) : TransportConfigCallback {
    override fun configure(transport: Transport?) {
        (transport as SshTransport).sshSessionFactory = object : JschConfigSessionFactory() {
            override fun configure(hc: OpenSshConfig.Host?, session: Session?) {
                session?.setConfig("StrictHostKeyChecking", "no")
            }

            override fun createDefaultJSch(fs: FS?): JSch = super.createDefaultJSch(fs).apply {
                addIdentity(sshKeyPath, sshPassPhrase)
            }
        }
    }
}
