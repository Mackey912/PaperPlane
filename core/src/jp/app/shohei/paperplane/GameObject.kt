package jp.app.shohei.paperplane

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2

open class GameObject(texture:Texture,secX:Int,srcY:Int,srcWidth:Int,srcHeight:Int)
    :Sprite(texture,secX,srcY,srcWidth,srcHeight){
    val velocity:Vector2
    init{
        velocity=Vector2()
    }
}