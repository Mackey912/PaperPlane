package jp.techacademy.shohei.paperplane

import com.badlogic.gdx.graphics.Texture

class Enemy(type:Int,texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val ENEMY_WIDTH = 1.3f
        val ENEMY_HEIGHT = 1.3f

        // タイプ（通常と動くタイプ）
        val ENEMY_TYPE_STATIC = 0
        val ENEMY_TYPE_MOVING = 1

        val ENEMY_VELOCITY=4.0f


    }
    var mType:Int
    var flip:Int=0

    init {
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT)
        mType = type
        if (mType == ENEMY_TYPE_MOVING) {
            velocity.x = ENEMY_VELOCITY
        }
    }

    // 座標を更新する
    fun update(deltaTime: Float) {
        if (mType == ENEMY_TYPE_MOVING) {
            x += velocity.x * deltaTime

            if (x < ENEMY_WIDTH / 2) {
                velocity.x = -velocity.x
                x = ENEMY_WIDTH / 2
                flip=0
            }
            if (x > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
                velocity.x = -velocity.x
                x = GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2
                flip=1
            }
        }
    }

}