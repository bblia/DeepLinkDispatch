/*
 * Copyright (C) 2015 Airbnb, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airbnb.deeplinkdispatch.sample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.airbnb.deeplinkdispatch.DeepLink

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.TaskStackBuilder

@DeepLink("dld://classDeepLink", "http://example.com/foo{arg}", "dld://example.com/deepLink")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            var toastMessage = ""
            val parameters = intent.extras
            Log.d(TAG, "Deeplink params: " + parameters!!)

            if (ACTION_DEEP_LINK_METHOD == intent.action) {
                toastMessage = "method with param1:" + parameters.getString("param1")!!
            } else if (ACTION_DEEP_LINK_COMPLEX == intent.action) {
                toastMessage = parameters.getString("arbitraryNumber")
            } else if (parameters.containsKey("arg")) {
                toastMessage = "class and found arg:" + parameters.getString("arg")!!
            } else {
                toastMessage = "class"
            }

            // You can pass a query parameter with the URI, and it's also in parameters, like
            // dld://classDeepLink?qp=123
            if (parameters.containsKey("qp")) {
                toastMessage += " with query parameter " + parameters.getString("qp")!!
            }
            val referrer = ActivityCompat.getReferrer(this)
            if (referrer != null) {
                toastMessage += " and referrer: " + referrer.toString()
            }

            showToast(toastMessage)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, "Deep Link: $message", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val ACTION_DEEP_LINK_METHOD = "deep_link_method"
        private val ACTION_DEEP_LINK_COMPLEX = "deep_link_complex"
        private val TAG = MainActivity::class.java!!.getSimpleName()

        /**
         * Handles deep link with one param, doc does not contain "param"
         * @return A intent to start [MainActivity]
         */
        @DeepLink("dld://methodDeepLink/{param1}")
        fun intentForDeepLinkMethod(context: Context): Intent {
            return Intent(context, MainActivity::class.java).setAction(ACTION_DEEP_LINK_METHOD)
        }


        @DeepLink("dld://host/somePath/{arbitraryNumber}")
        fun intentForParamDeepLinkMethod(context: Context): Intent {
            return Intent(context, MainActivity::class.java).setAction(ACTION_DEEP_LINK_COMPLEX)
        }

        /**
         * Handles deep link with params.
         * @param context of the activity
         * @param bundle expected to contain the key `qp`.
         * @return TaskStackBuilder set with first intent to start [MainActivity] and second intent
         * to start [SecondActivity].
         */
        @DeepLink("http://example.com/deepLink/{id}/{name}/{place}")
        fun intentForTaskStackBuilderMethods(context: Context, bundle: Bundle?): TaskStackBuilder {
            Log.d(TAG, "without query parameter :")
            if (bundle != null && bundle.containsKey("qp")) {
                Log.d(TAG, "found new parameter :with query parameter :" + bundle.getString("qp")!!)
            }
            val detailsIntent =
                Intent(context, SecondActivity::class.java).setAction(ACTION_DEEP_LINK_COMPLEX)
            val parentIntent =
                Intent(context, MainActivity::class.java).setAction(ACTION_DEEP_LINK_COMPLEX)
            val taskStackBuilder = TaskStackBuilder.create(context)
            taskStackBuilder.addNextIntent(parentIntent)
            taskStackBuilder.addNextIntent(detailsIntent)
            return taskStackBuilder

        }

        @DeepLink("dld://host/somePathOne/{arbitraryNumber}/otherPath")
        fun intentForComplexMethod(context: Context, bundle: Bundle?): Intent {
            if (bundle != null && bundle.containsKey("qp")) {
                Log.d(TAG, "found new parameter :with query parameter :" + bundle.getString("qp")!!)
            }
            return Intent(context, MainActivity::class.java).setAction(ACTION_DEEP_LINK_COMPLEX)
        }
    }
}
