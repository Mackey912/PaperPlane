package jp.app.shohei.paperplane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import kotlin.math.*

class Player(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val PLAYER_WIDTH = 0.8f
        val PLAYER_HEIGHT = 1.0f
        // 速度
        val PLAYER_MOVE_VELOCITY = 11.0f
    }

    private var prevangleKey:Int
    private val angleInterval:Float

    init {
        setSize(PLAYER_WIDTH, PLAYER_HEIGHT)
        prevangleKey=0
        angleInterval=18f
    }

    fun update(delta: Float, angleKey: Int) {

        // 角度情報によって座標変更
        velocity.y = -0.5f *cos((angleKey.toFloat()*angleInterval)/180f*PI).toFloat()*GameScreen.fall_velocity
        velocity.x = -0.5f *sin((angleKey.toFloat()*angleInterval)/180f*PI).toFloat()* GameScreen.fall_velocity
        setPosition(x + velocity.x * delta, y + velocity.y * delta)
        setOrigin(PLAYER_WIDTH/2, PLAYER_HEIGHT/2)
        rotate(-angleInterval*(angleKey-prevangleKey).toFloat())

        prevangleKey=angleKey


    }
}