package com.advait.bmwproject.viewmodel

import android.app.Application
import android.provider.Settings.Secure.ANDROID_ID
import androidx.lifecycle.AndroidViewModel
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class BitcoinMineViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var inputHashData: String
    private lateinit var bitcoin: BigDecimal
    private var isLoggedIn = false

    private val BACKEND_URL = "https://bitcoin-backend.herokuapp.com/"

    fun addClient(): Boolean {
        Volley.newRequestQueue(getApplication()).add(JsonObjectRequest(
            Request.Method.POST, BACKEND_URL + "client/addClient?clientId=" + ANDROID_ID, null,
            Response.Listener<JSONObject> {
                isLoggedIn = true
            }, Response.ErrorListener { isLoggedIn = false })
        )
        return isLoggedIn
    }

    fun login(): Boolean {
        if(!isLoggedIn) {
            Volley.newRequestQueue(getApplication()).add(JsonObjectRequest(
                Request.Method.POST, BACKEND_URL + "client/login?clientId=" + ANDROID_ID, null,
                Response.Listener<JSONObject> {
                    isLoggedIn = true
                }, Response.ErrorListener { isLoggedIn = false })
            )
            return isLoggedIn
        } else {
            return isLoggedIn
        }
    }


    fun fetchWork(): Boolean {
        var status = false
        Volley.newRequestQueue(getApplication()).add(StringRequest(BACKEND_URL + "work",
            Response.Listener<String> { response ->
                val responseJson = JSONObject(response)
                val hashMessage: JSONObject? = responseJson.optJSONObject("blockHeader")
                inputHashData =
                    hashMessage?.run { optString("prevBlockHash") } +
                            hashMessage?.run { optString("nonce") }
                status = true
            }, Response.ErrorListener { status = false })
        )

        return status
    }

    private fun sendCalculatedHashToCloud(hashOutput: String) {
        //connect to cloud
    }

    private fun calculateHash(inputString: String): String {
        return toHexString(getSHA(inputString))
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    @Throws(NoSuchAlgorithmException::class)
    fun getSHA(input: String): ByteArray {
        // Static getInstance method is called with hashing SHA
        val md = MessageDigest.getInstance("SHA-256")

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.encodeToByteArray())
    }

    fun toHexString(hash: ByteArray): String {
        // Convert byte array into signum representation
        val number = BigInteger(1, hash)

        // Convert message digest into hex value
        val hexString = StringBuilder(number.toString(16))

        // Pad with leading zeros
        while (hexString.length < 32) {
            hexString.insert(0, '0')
        }

        return hexString.toString()
    }

    fun calculateHashAndSendToCloud() {
        if (inputHashData.isNotEmpty()) {
            val hashOutput = calculateHash(inputHashData)
            if (hashOutput.isNotEmpty()) {
                sendCalculatedHashToCloud(hashOutput)
            }
        }
    }

    fun isInputHashDataAvailable():Boolean {
        return inputHashData.isNotEmpty()
    }
}