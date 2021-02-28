package dagger

import json.MoshiModule
import json.slack.message.MessageTypeRecorder
import network.http.HttpUtils
import network.http.HttpUtilsBasic
import slack.Settings
import slack.SlackData
import slack.SlackExport
import slack.SlackExportProcessor
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

  fun getMessageTypeRecorder(): MessageTypeRecorder

  @Module
  interface Declarations {
    @Binds
    fun provideHttpUtils(httpUtilsBasic: HttpUtilsBasic): HttpUtils

    @Binds
    fun bindSlackData(slackExport: SlackExport): SlackData
  }
}

