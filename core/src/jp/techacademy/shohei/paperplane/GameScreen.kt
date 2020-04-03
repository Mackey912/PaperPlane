package jp.techacademy.shohei.paperplane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

class GameScreen(private val mGame: PaperPlane) : ScreenAdapter() {
    companion object {

        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f
        val WORLD_WIDTH = 10f
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f

        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2
        val START_POSITION_X=2f
        val START_POSITION_Y=6f
        val MAIN_STAGE_HEIGHT=15f*5f
        val SUB_STAGE_HEIGHT=15f*2f

        val RIGHT=0
        val LEFT=1
        val SEPARATE=2

        // 横幅、高さ
        val PLAYER_WIDTH = 1.0f
        val PLAYER_HEIGHT = 1.0f
        var fcount:Int=0
        var block_height:Float=1f
        var block_width:Float=1f
        var world_height:Float=15f*5f
        var fall_velocity:Float=11f

    }

    private var mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewPort: FitViewport

    private var mRandom: Random
    private var mBlocks:ArrayList<Block>
    private var mFragments:ArrayList<Fragment>
    private lateinit var mPlayer: Player

    private var mGameState: Int
    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3
    private var mFont:BitmapFont
    private var mFont_gameover:BitmapFont
    private var mScore:Int
    private var mHighScore:Int
    private var mPrefs:Preferences
    //どの角度を保持しているか-2～+2
    private var angleKey:Int=0
    private var pangleKey:Int=0
    private var time:Float=0f
    private var touchInterval:Float=0f
    private var prevAngleState:Boolean=false
    private var stageFlag:Boolean=false
    private var stageCount:Int=0
    private var y:Float=0f
    private var stepInterval= 5f
    private var block_height_count:Int=0
    private var pStageState= RIGHT


    // テクスチャの準備
    private val blockTexture = Texture("kikagaku.png")
    private val blockTexture2 = Texture("wall2.png")
    private val playerTexture = Texture("paperplane2.png")


    //音の設定
    val sound_pera=Gdx.audio.newSound(Gdx.files.internal("pera.mp3"))
    val sound_hitstar=Gdx.audio.newSound(Gdx.files.internal("sound_hitstar.mp3"))
    val sound_background=Gdx.audio.newMusic(Gdx.files.internal("background.mp3"))
    val sound_gotostartmenu=Gdx.audio.newSound(Gdx.files.internal("gotostartmenu.mp3"))

    // 背景の準備
    val bgTexture_blue = Texture("blue.png")
    val bgTexture_yellow = Texture("yellow.png")
    val bgTexture_orange = Texture("orange.png")
    val bgTexture_black = Texture("black2.jpg")
    val bgTexture_navy = Texture("navy.jpg")



    init {

        // TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture_blue, 300, 0, 540, 810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f, 0f)

        // カメラ、ViewPortを生成、設定する
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // プロパティの初期化
        mRandom = Random()
        mBlocks=ArrayList<Block>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()
        mFragments=ArrayList<Fragment>()
        mFont= BitmapFont(Gdx.files.internal("font.fnt"),Gdx.files.internal("font.png"),false)
        mFont.data.setScale(0.8f)
        mFont_gameover= BitmapFont(Gdx.files.internal("font.fnt"),Gdx.files.internal("font.png"),false)
        mFont_gameover.data.setScale(1.3f)
        mFont_gameover.setColor(Color.PINK)
        mScore=3
        mHighScore=0

        //ハイスコアをPreferencesから取得する
        mPrefs=Gdx.app.getPreferences("jp.techacademy.shohei.paperplane")
        mHighScore=mPrefs.getInteger("HIGHSCORE",0)
        sound_background.setVolume(0.2f)
        sound_background.setLooping(true)
        sound_background.play()

        createStage()
    }

    override fun render(delta: Float) {
        // それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.y < mCamera.position.y) {
            mCamera.position.y = mPlayer.y
        }


        //Gdx.app.log("mPlayer.y",mPlayer.y.toString())
        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin()

        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // Step
        for (i in 0 until mBlocks.size) {
            mBlocks[i].draw(mGame.batch)
        }

        //Player
        if(mGameState== GAME_STATE_PLAYING||mGameState== GAME_STATE_READY) {
            mPlayer.draw(mGame.batch)
        }else if(mGameState== GAME_STATE_GAMEOVER){
            for(i in 0 until mFragments.size) {
                mFragments[i].draw(mGame.batch)
            }
        }

        mGame.batch.end()

        //スコア表示

        mScore=-(mPlayer.y*0.2f).toInt()
        mScore+=1
        if (mScore > mHighScore) {
            mHighScore = mScore
            mPrefs.putInteger("HIGHSCORE",mHighScore)
            mPrefs.flush()
        }
        mGuiCamera.update()
        mGame.batch.projectionMatrix=mGuiCamera.combined
        mGame.batch.begin()
        mFont.draw(mGame.batch,"HighScore:$mHighScore",16f, GUI_HEIGHT-15)
        mFont.draw(mGame.batch,"Score:$mScore",16f, GUI_HEIGHT-35)
        if(mGameState== GAME_STATE_GAMEOVER){
            mFont_gameover.draw(mGame.batch,"GAME OVER",55f, GUI_HEIGHT/2+60f)
        }
        mGame.batch.end()

        //次ステージ作成

        if(abs(mPlayer.y)% (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)/(MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)>0.6f&&stageFlag==true){
            stageCount++
            createStage()
            stageFlag=false
        }
        if(abs(mPlayer.y)% (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)/ (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)<0.2f&&stageFlag==false){
            stageFlag=true
        }
        var nowStage=abs(mPlayer.y)/ (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)
        if(2>nowStage&&nowStage>=1){
            mBg = Sprite(TextureRegion(bgTexture_yellow, 300, 0, 540, 810))
            mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
            mBg.setPosition(0f, 0f)
        }else if(3>nowStage&&nowStage>=2){
            mBg = Sprite(TextureRegion(bgTexture_orange, 300, 0, 540, 810))
            mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
            mBg.setPosition(0f, 0f)
        }else if(4>nowStage&&nowStage>=3){
            mBg = Sprite(TextureRegion(bgTexture_navy, 0, 0, 405, 720))
            mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
            mBg.setPosition(0f, 0f)
        } else if(nowStage>=4){
            mFont_gameover.setColor(Color.GOLD)
            mBg = Sprite(TextureRegion(bgTexture_black, 0, 0, 540, 810))
            mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
            mBg.setPosition(0f, 0f)
        }

    }

    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height)
        mGuiViewPort.update(width, height)
    }

    // ステージを作成する
    private fun createStage() {

        // Stepをゴールの高さまで配置していく


        if(stageCount==0) {
            // Playerを配置
            var y = 0f
            mCamera.position.y = 0f

            //初めに隙間が空かないように壁を作っておく
            block_width=0.5f
            block_height=stepInterval*2
            var wallR = Block(blockTexture2, 0, 0, 50, 349)
            wallR.setPosition(0f, 0f)
            mBlocks.add(wallR)

            block_width=0.5f
            block_height=stepInterval*2
            var wallL = Block(blockTexture2, 0, 0, 50, 349)
            wallL.setPosition(WORLD_WIDTH-0.5f, 0f)
            mBlocks.add(wallL)
        }
        /*


            //縦の拡大が大きいので，開始位置を少し下げる
            y-=4f
         */
        if(stageCount<=2){
            block_height_count=stageCount
        }else{
            fall_velocity+=0.7f
        }

        while (-(stageCount+1).toFloat()* (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)<=y&&y <= -stageCount.toFloat()* (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)) {

            if (abs(y)% (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)<MAIN_STAGE_HEIGHT) {


                if(pStageState==LEFT&&mRandom.nextFloat()<0.8){
                    setRightStage()
                    pStageState=RIGHT
                }else if(pStageState==RIGHT&&mRandom.nextFloat()<0.8){
                    setLeftStage()
                    pStageState=LEFT
                }else{
                    setSeparateStage()
                }

                block_width=0.5f
                block_height=stepInterval
                val wallR = Block(blockTexture2, 0, 0, 50, 349)
                wallR.setPosition(0f, y)
                mBlocks.add(wallR)

                block_width=0.5f
                block_height=stepInterval
                val wallL = Block(blockTexture2, 0, 0, 50, 349)
                wallL.setPosition(WORLD_WIDTH-0.5f, y)
                mBlocks.add(wallL)

                y -= stepInterval

                /*
                setLeftStage()
                setRightStage()
                setSeparateStage()

                 */

                if(abs(y)% (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)>=MAIN_STAGE_HEIGHT){
                    //MAINとSUBの間はステージを少し開ける
                    block_width=0.5f
                    block_height=stepInterval
                    val wallR = Block(blockTexture2, 0, 0, 50, 349)
                    wallR.setPosition(0f, y)
                    mBlocks.add(wallR)

                    block_width=0.5f
                    block_height=stepInterval
                    val wallL = Block(blockTexture2, 0, 0, 50, 349)
                    wallL.setPosition(WORLD_WIDTH-0.5f, y)
                    mBlocks.add(wallL)
                    y-=4f
                }

            } else if(abs(y)% (MAIN_STAGE_HEIGHT+ SUB_STAGE_HEIGHT)>=MAIN_STAGE_HEIGHT){
                //一つ目のブロック0~x1,二つ目のブロックx2~WIDTHに分割
                val x1 = WORLD_WIDTH / 3
                val blockInterval = WORLD_WIDTH / 3
                val x2 = x1 + blockInterval

                block_width=x1
                block_height=stepInterval-0.1f
                val blockR = Block(blockTexture, 0, 0, 50* block_width.toInt(), 50* block_height.toInt())
                blockR.setPosition(0f, y)
                mBlocks.add(blockR)

                block_width=x1
                block_height=stepInterval-0.1f
                val blockL = Block(blockTexture, 0, 0, 50* block_width.toInt(), 50* block_height.toInt())
                blockL.setPosition(x2, y)
                mBlocks.add(blockL)

                y -= stepInterval
            }
        }
        //ステージ間で隙間が空かないようにする
        block_width=0.5f
        block_height=stepInterval
        var wallR = Block(blockTexture2, 0, 0, 50* block_width.toInt(), 50* block_height.toInt())
        wallR.setPosition(0f, y)
        mBlocks.add(wallR)

        block_width=0.5f
        block_height=stepInterval
        var wallL = Block(blockTexture2, 0, 0, 50* block_width.toInt(), 50* block_height.toInt())
        wallL.setPosition(WORLD_WIDTH-0.5f, y)
        mBlocks.add(wallL)

        //古いのは削除
        while(mBlocks.size>120){
            mBlocks.removeAt(0)
        }
    }

    private fun setSeparateStage(){
        //一つ目のブロック0~x1,二つ目のブロックx2~WIDTHに分割
        val x1 = mRandom.nextFloat() * WORLD_WIDTH / 3 + WORLD_WIDTH / 6
        val blockInterval = mRandom.nextFloat() * 3 / 5 + WORLD_WIDTH / 4
        val x2 = x1 + blockInterval

        block_width=x1-0.5f
        block_height=0.8f*(block_height_count.toFloat()+1f)
        val blockR = Block(blockTexture, 0, 0, (50* block_width).toInt(), (50f* block_height).toInt())
        blockR.setPosition(0f+0.5f, y)
        mBlocks.add(blockR)

        block_width= WORLD_WIDTH-x2-0.5f
        block_height=0.8f*(block_height_count.toFloat()+1f)
        val blockL = Block(blockTexture, 0, 0, (50* block_width).toInt(), (50f* block_height).toInt())
        blockL.setPosition(x2, y)
        mBlocks.add(blockL)
    }

    private fun setLeftStage(){

        val x=mRandom.nextFloat()* WORLD_WIDTH/5+ WORLD_WIDTH/2-1f
        block_width=x-0.5f
        block_height=0.8f*(block_height_count.toFloat()+1f)
        val blockL = Block(blockTexture, 0, 0, (50* block_width).toInt(), (50f* block_height).toInt())
        blockL.setPosition(0f+0.5f, y)
        mBlocks.add(blockL)

    }

    private fun setRightStage(){

        val x=mRandom.nextFloat()* WORLD_WIDTH/5+ WORLD_WIDTH/2-1f
        block_width= (WORLD_WIDTH-x)-0.5f
        block_height=0.8f*(block_height_count.toFloat()+1f)
        val blockR = Block(blockTexture, 0, 0, (50* block_width).toInt(), (50f* block_height).toInt())
        blockR.setPosition(x, y)
        mBlocks.add(blockR)

    }



    // それぞれのオブジェクトの状態をアップデートする
    private fun update(delta: Float) {
        when (mGameState) {

            GAME_STATE_READY ->
                updateReady(delta)
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver(delta)
        }
    }

    private fun updateReady(delta:Float) {
        mPlayer = Player(playerTexture, 0, 0, 87, 113)
        mPlayer.setPosition(START_POSITION_X, START_POSITION_Y)
        angleKey=-3
        mPlayer.update(delta, 4)
        mGameState = GAME_STATE_PLAYING
    }

    private fun updatePlaying(delta: Float) {

        time += Gdx.graphics.getDeltaTime()
        touchInterval += Gdx.graphics.getDeltaTime()
        //Gdx.app.log("time", time.toString())

        //タッチ感覚の調整
        if (touchInterval >= 3f * Gdx.graphics.getDeltaTime()) {
            if (Gdx.input.isTouched) {
                mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
                val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
                val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
                if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                    if (angleKey != 4) angleKey++
                }
                if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                    if (angleKey != -4) angleKey--
                }
                if (pangleKey == angleKey) {
                    //前と同じ角度なら音を鳴らさない
                } else if (prevAngleState==true&&angleKey>pangleKey==false){
                    //右タップの後に左タップした場合音を鳴らす
                    sound_pera.play(0.4f)
                } else if (prevAngleState==false&&angleKey>pangleKey==true){
                    //タップの後に左右タップした場合音を鳴らす
                    sound_pera.play(0.4f)
                } else if (touchInterval <= 6f * Gdx.graphics.getDeltaTime()) {
                    //連続タップ時音を鳴らさない
                } else {
                    sound_pera.play(0.4f)
                }
                prevAngleState=angleKey>pangleKey
                pangleKey = angleKey
                touchInterval = 0f
            }
        }

        mPlayer.update(delta, angleKey)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // 当たり判定を行う
        checkCollision()

    }



    private fun updateGameOver(delta:Float) {
        for(fragment in mFragments) {
            fragment.update(delta)
        }
        if(Gdx.input.justTouched()){
            sound_gotostartmenu.play(0.4f)
            sound_background.stop()
            mGame.screen=StartScreen(mGame)
        }
    }

    private fun checkCollision() {

        // Stepとの当たり判定
        for (i in 0 until mBlocks.size) {
            val block = mBlocks[i]
            val mPlayer_small=Rectangle()
            //当たり判定を修正する
            mPlayer_small.x=mPlayer.x+0.35f
            mPlayer_small.y=mPlayer.y+1f/3
            mPlayer_small.width=0.1f
            mPlayer_small.height=1f/3

            if (block.boundingRectangle.overlaps(mPlayer_small)) {
                sound_hitstar.play(0.4f)

                //死亡時，破片をまき散らす
                val fragmentTexture = Texture("fragment.png")
                for(i in 0 until 30){
                    var fragment=Fragment(fragmentTexture, 0, 0, 16, 16)
                    fragment.setPosition(mPlayer.x,mPlayer.y)
                    fragment.setup()
                    mFragments.add(fragment)
                    fcount++
                }
                mGameState= GAME_STATE_GAMEOVER
            }
        }
    }
}