package network.http

import dagger.Binds
import dagger.Module

object HttpUtilsModule {
  @Module
  interface Basic {
    @Binds
    fun bindHttpUtilsBasic(httpUtils: HttpUtilsBasic): HttpUtils
  }

  @Module
  interface Token {
    @Binds
    fun bindHttpUtilsToken(httpUtils: HttpUtilsToken): HttpUtils
  }
}
