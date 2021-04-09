pluginManagement{
    repositories{
        gradlePluginPortal()
        jcenter()
    }
    resolutionStrategy{
        eachPlugin{
            if(requested.id.id.startsWith("org.jetbrains.kotlin")) useVersion("1.3.72")
            if(requested.id.id.startsWith("org.springframework.boot")) useVersion("2.2.9.RELEASE")
        }
    }
}
rootProject.name = "interview"
