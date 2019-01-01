package slackjson

/**
 * Base class when parsing any slack file object
 * Most of the time should be an instance of ParsedFile, but could not be in certain cases (eg. Tombstone)
 */
abstract class BaseFile {
    abstract val mode: String

    fun isTombstone() = mode == "tombstone"
}