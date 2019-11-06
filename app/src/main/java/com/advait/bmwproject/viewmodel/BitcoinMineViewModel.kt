package com.advait.bmwproject.viewmodel

import android.app.Application
import android.provider.Settings.Secure.ANDROID_ID
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class BitcoinMineViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var inputHashData: String
    private var bitcoin:MutableLiveData<Int> = MutableLiveData()
    private var isLoggedIn = false

    private val BACKEND_URL = "https://bitcoin-backend.herokuapp.com/"
    /**
     * future-use for registering new client for authentication purpose
     */
    fun addClient(): Boolean {
        Volley.newRequestQueue(getApplication()).add(JsonObjectRequest(
            Request.Method.POST, BACKEND_URL + "client/addClient?clientId=$ANDROID_ID" , null,
            Response.Listener<JSONObject> {
                isLoggedIn = true
            }, Response.ErrorListener { isLoggedIn = false })
        )
        return isLoggedIn
    }

    /**
     * logging the client in the server
     */
    fun login(): Boolean {
        if (!isLoggedIn) {
            Volley.newRequestQueue(getApplication()).add(StringRequest(
                BACKEND_URL + "client/login?clientId=$ANDROID_ID",
                Response.Listener<String> {
                    isLoggedIn = true
                }, Response.ErrorListener { isLoggedIn = false })
            )
            return isLoggedIn
        } else {
            return isLoggedIn
        }
    }

    /**
     * fetching the input for computing hash
     */
    fun fetchWork(): Boolean {
        var status = false
        Volley.newRequestQueue(getApplication()).add(StringRequest(BACKEND_URL + "work",
            Response.Listener<String> { response ->
                val responseJson = JSONObject(response)
                val hashMessage: JSONObject? = responseJson.optJSONObject("blockHeader")
                inputHashData = hashMessage?.run { optString("merkleRoot") } +
                        hashMessage?.run { optString("prevBlockHash") } +
                        hashMessage?.run { optString("nonce") }
                status = true
            }, Response.ErrorListener { status = false })
        )

        return status
    }

    /**
     * method to submit calculated hash to cloud
     */
    private fun sendCalculatedHashToCloud(hashOutput: String) {
        Volley.newRequestQueue(getApplication()).add(
            JsonObjectRequest(
                Request.Method.POST,
                BACKEND_URL + "submit?hash=" + hashOutput + "&clientId=" + ANDROID_ID,
                null,
                Response.Listener<JSONObject> {
                    bitcoin.value = bitcoin.value?.plus(1)
                },
                Response.ErrorListener { })
        )
    }

    /**
     * method to calculate hash
     */
    private fun calculateHash(inputString: String): String {
        return toHexString(getSHA(inputString))
    }

    /**
     * method to computer hash from input string to byte array
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    @Throws(NoSuchAlgorithmException::class)
    fun getSHA(input: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(input.encodeToByteArray())
    }

    /**
     * method to convert byte array to hex string.
     */
    fun toHexString(hash: ByteArray): String {
        val number = BigInteger(1, hash)

        val hexString = StringBuilder(number.toString(16))

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

    fun isInputHashDataAvailable(): Boolean {
        return inputHashData.isNotEmpty()
    }

    public fun getBitCoinCount():MutableLiveData<Int> {
        return bitcoin
    }
}