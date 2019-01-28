/**
 * Copyright 2016 Bryan Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.btkelly.gnag.api;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User-Agent header is required by GitHub API v3, see
 * https://developer.github.com/v3/#user-agent-required
 */
public class UserAgentInterceptor implements Interceptor {

  @Override
  public Response intercept(final Chain chain) throws IOException {
    final Request request = chain.request()
                                 .newBuilder()
                                 .addHeader("User-Agent", "btkelly-gnag")
                                 .build();

    return chain.proceed(request);
  }

}
