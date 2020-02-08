package bask.lingvino.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import okhttp3.*
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * This class handles pronunciations by using Microsoft Azure Cognitive Services.
 */
class CognitiveServices(val context: Context) {

    private var httpClient: OkHttpClient = OkHttpClient()
    private lateinit var accessToken: String
    private var remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private lateinit var mediaPlayer: MediaPlayer // For playing text pronunciations.

    init {
        getSpeechAccessToken(remoteConfig.getString("SPEECH_SERVICE_API_KEY"))
        { token -> accessToken = token!! }
    }

    // Retrieve access token for Speech API.
    private fun getSpeechAccessToken(key: String, callback: (String?) -> Unit) {
        val request = Request.Builder()
            .url("https://northeurope.api.cognitive.microsoft.com/sts/v1.0/issuetoken")
            .addHeader("Ocp-Apim-Subscription-Key", key)
            .post(RequestBody.create(null, ""))
            .build()

        // Async http POST request.
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("accessToken").d(e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Pass token to callback.
                callback(response.body()?.string())
            }

        })
    }

    // Takes input text and synthesizes it into speech.
    fun pronounceText(text: String, language: String) {
        // Default values.
        var name = "en-GB-George-Apollo"
        var lang = "en-GB"

        // Determine what language and which speaker to use for pronunciations.
        when (language) {
            "English" -> {
                name = "en-GB-George-Apollo"
                lang = "en-GB"
            }
            "Bulgarian" -> {
                name = "bg-BG-Ivan"
                lang = "bg-BG"
            }
            "Spanish" -> {
                name = "es-ES-Pablo-Apollo"
                lang = "es-ES"
            }
            "Russian" -> {
                name = "ru-RU-Pavel-Apollo"
                lang = "ru-RU"
            }
        }

        // Construct XML body for the HTTP request.
        val body = xml("speak") {
            version = XmlVersion.V10
            attribute("version", "1.0")
            attribute("xml:lang", lang)
            "voice" {
                attribute("name", name)
                attribute("xml:gender", "Male")
                attribute("xml:lang", lang)
                -text
            }
        }.toString()

        val request = Request.Builder()
            .url("https://northeurope.tts.speech.microsoft.com/cognitiveservices/v1")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/ssml+xml")
            .addHeader("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3")
            .addHeader("cache-control", "no-cache")
            .post(RequestBody.create(MediaType.parse("stream"), body))
            .build()

        // Async HTTP POST request that fetches an audio file that will be played on user's device.
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("pronounce").d(e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Get the audio response and save the file to temp storage.
                val inputStream = response.body()?.byteStream()
                val file = File.createTempFile("speech", ".mp3", context.obbDir)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                outputStream.close()

                // Create MediaPlayer instance that uses the audio result from HTTP request.
                mediaPlayer =
                    MediaPlayer.create(context, Uri.fromFile(file))
                        .apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                        }

                // Play the pronunciation of the translated text.
                mediaPlayer.start()
            }
        })
    }
}