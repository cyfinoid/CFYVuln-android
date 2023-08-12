package cfy.vuln.app



import android.os.Build
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

/**
 * This program demonstrates how to
 * encrypt/decrypt input using the Blowfish
 * Cipher with the Java Cryptograhpy.
 * */

//Encryption decryption Function
class Utils {
    var key="CFYvulnerbilityApp"
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        UnsupportedEncodingException::class
    )
    fun encrypt(password: String, key: String): String {
        val KeyData = key.toByteArray()
        val KS = SecretKeySpec(KeyData, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.ENCRYPT_MODE, KS)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(cipher.
            doFinal(password.toByteArray(charset("UTF-8"))))
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun decrypt(encryptedtext: String?, key: String): String {
        val KeyData = key.toByteArray()
        val KS = SecretKeySpec(KeyData, "Blowfish")
        val ecryptedtexttobytes = Base64.getDecoder().
        decode(encryptedtext)
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.DECRYPT_MODE, KS)
        val decrypted = cipher.doFinal(ecryptedtexttobytes)
        return String(decrypted, Charset.forName("UTF-8"))
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }
}
