package no.elhub.tools.autorelease

enum class VersionBump {
    NONE,
    PRERELEASE,
    PATCH,
    MINOR,
    MAJOR;

    companion object {

        fun analyze(messages: List<String>): VersionBump {
            var bumpType = NONE
            messages.reversed().forEach { message ->
                when {
                    message.contains(Configuration.majorPattern) -> return MAJOR
                    message.contains(Configuration.minorPattern) -> {
                        if (bumpType < MINOR)
                            bumpType = MINOR
                    }
                    message.contains(Configuration.patchPattern) -> {
                        if (bumpType < PATCH)
                            bumpType = PATCH
                    }
                    message.contains(Configuration.prereleasePattern) -> {
                        if (bumpType < PRERELEASE)
                            bumpType = PRERELEASE
                    }
                    else -> Unit
                }
            }
            return bumpType
        }

    }

}
