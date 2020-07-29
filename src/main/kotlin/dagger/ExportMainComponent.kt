package dagger

import network.http.HttpUtils
import network.http.HttpUtilsBasic
import slack.Settings
import slack.SlackData
import slack.SlackExport
import slack.SlackExportProcessor
import slackjson.MoshiModule
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            ExportMainComponent.Declarations::class,
            SlackExport.Provider::class,
            MoshiModule::class
        ]
)
interface ExportMainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun settings(settings: Settings): Builder

        @BindsInstance
        fun folderLocation(@Named("FolderLocation") location: Path): Builder

        fun build(): ExportMainComponent
    }

    @Deprecated(message = "To be replaced with something better I hope")
    fun getUserAndConvoMap(): SlackData

    fun getExportProcessor(): SlackExportProcessor

    @Module
    interface Declarations {
        @Binds
        fun provideHttpUtils(httpUtilsBasic: HttpUtilsBasic): HttpUtils

        @Binds
        fun bindSlackData(slackExport: SlackExport): SlackData
    }
}

