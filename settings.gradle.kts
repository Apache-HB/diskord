rootProject.name = "strife"

include(
    ":client",
    ":addons:commands", ":addons:memory", ":addons:scripting",
    ":samples:ping", ":samples:embeds",
    ":samples:commands-addon", ":samples:memory-addon", ":samples:scripting-addon"
)
