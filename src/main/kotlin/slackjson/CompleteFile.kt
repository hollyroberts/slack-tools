package slackjson

class CompleteFile(sf: SlackFile, val uploadLoc: String?) : SlackFile() {
    override val id = sf.id
    override val user = sf.user
    override val title = sf.title

    override val mode = sf.mode
    override val filetype = sf.filetype
    override val size = sf.size
    override val timestamp = sf.timestamp
    override val urlPrivate = sf.urlPrivate
    override val urlPrivateDownload = sf.urlPrivateDownload

    // Lists can be copied by reference as they're not mutable (map is also immutable)
    override val channels = sf.channels
    override val groups = sf.groups
    override val ims = sf.ims

    override var settings = sf.settings
    override var slackData = sf.slackData
}