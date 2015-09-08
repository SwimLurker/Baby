package org.slstudio.baby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.rsp.IRSPListener;
import org.slstudio.baby.game.rsp.RSPGame;
import org.slstudio.baby.game.rsp.RSPProfile;
import org.slstudio.baby.game.rsp.RSPProfileFactory;
import org.slstudio.baby.game.rsp.RSPResult;
import org.slstudio.baby.game.rsp.RSPType;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class RSPGameActivity extends GameActivity<RSPGame, RSPProfile> implements IRSPListener{
	
	public static final int SFX_GAMESTART = 1;
	public static final int SFX_WIN = 2;
	public static final int SFX_LOSE = 3;
	public static final int SFX_ROUND1 = 4;
	public static final int SFX_ROUND2 = 5;
	public static final int SFX_ROUND3 = 6;
	public static final int SFX_ROUND4 = 7;
	public static final int SFX_ROUND5 = 8;
	public static final int SFX_ROUNDFINAL = 9;
	public static final int SFX_CHOOSE = 10;
	public static final int SFX_SHOWRESULT = 11;
	
	public static final int MAX_LIFT = 100;
	
	private Resources resources = null;
	
	private int screenWidth = 0;
	private int screenHeight = 0;
	
	private ProgressBar p1LifeBarPB = null;
	private ProgressBar p2LifeBarPB = null;
	private TextView p1LifeBarNameTV = null;
	private TextView p2LifeBarNameTV = null;
	private ImageView vsMsgIV = null;
	private TextView p1VSNameTV = null;
	private TextView p2VSNameTV = null;
	private RelativeLayout vsMsgLayout = null;
	private TextView roundNumberTV = null;
	private RelativeLayout roundMsgLayout = null;
	private GridView p1CardTableGV = null;
	private GridView p2CardTableGV = null;
	private RelativeLayout p1CardTableLayout = null;
	private RelativeLayout p2CardTableLayout = null;
	private RelativeLayout fightMsgLayout = null;
	private RelativeLayout resultMsgLayout = null;
	private ImageView resultMsgIV = null;
	private RelativeLayout p1ChoiceLayout = null;
	private ImageView p1ChoiceIV = null;
	private RelativeLayout p2ChoiceLayout = null;
	private ImageView p2ChoiceIV = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_rsp);
		
		resources = getResources();
		WindowManager wm = this.getWindowManager();
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		
		
		p1LifeBarPB = (ProgressBar)findViewById(R.id.game_rsp_p1_lifebar);
		p2LifeBarPB = (ProgressBar)findViewById(R.id.game_rsp_p2_lifebar);
		p1LifeBarNameTV = (TextView)findViewById(R.id.game_rsp_p1_lifebar_name);
		p2LifeBarNameTV = (TextView)findViewById(R.id.game_rsp_p2_lifebar_name);
		vsMsgLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_vsmsg);
		p1VSNameTV = (TextView)findViewById(R.id.game_rsp_vsmsg_p1name);
		p2VSNameTV = (TextView)findViewById(R.id.game_rsp_vsmsg_p2name);
		vsMsgIV = (ImageView)findViewById(R.id.game_rsp_vsmsg_vs);
		roundMsgLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_roundmsg);
		roundNumberTV = (TextView)findViewById(R.id.game_rsp_roundmsg_roundnumber);
		p1CardTableGV = (GridView)findViewById(R.id.game_rsp_p1_cardtable);
		p2CardTableGV = (GridView)findViewById(R.id.game_rsp_p2_cardtable);
		p1CardTableLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_p1_cardtable);
		p2CardTableLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_p2_cardtable);
		fightMsgLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_fightmsg);
		resultMsgLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_resultmsg);
		resultMsgIV = (ImageView)findViewById(R.id.game_rsp_resultmsg);
		p1ChoiceLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_p1choice);
		p2ChoiceLayout = (RelativeLayout)findViewById(R.id.game_rsp_layout_p2choice);
		p1ChoiceIV = (ImageView)findViewById(R.id.game_rsp_p1_choice);
		p2ChoiceIV = (ImageView)findViewById(R.id.game_rsp_p2_choice);
		
		LayoutParams lp = p1LifeBarPB.getLayoutParams();
		lp.width = (int)(screenWidth * 0.5);
		p1LifeBarPB.setLayoutParams(lp);
		
		p1LifeBarPB.setMax(MAX_LIFT);
		p1LifeBarPB.setProgress(MAX_LIFT);
		
		LayoutParams lp2 = p2LifeBarPB.getLayoutParams();
		lp2.width = (int)(screenWidth * 0.5);
		p2LifeBarPB.setLayoutParams(lp2);
		
		p2LifeBarPB.setMax(MAX_LIFT);
		p2LifeBarPB.setProgress(MAX_LIFT);
		
		p1LifeBarNameTV.setText("P1");
		p2LifeBarNameTV.setText("COM");
		
		p1VSNameTV.setText("P1");
		p2VSNameTV.setText("COM");
		
		
		final int[] p1CardImages = new int[]{R.drawable.card_rock, R.drawable.card_scissors, R.drawable.card_paper};
		
		
		ArrayList<HashMap<String, Object>> p1Cards = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<p1CardImages.length; i++){
			HashMap<String, Object> card = new HashMap<String, Object>();
			card.put("cardImage", p1CardImages[i]);
			p1Cards.add(card);
		}
		
		SimpleAdapter p1CardAdapter = new SimpleAdapter(this, p1Cards, R.layout.cardtable_item, 
				new String[]{"cardImage"}, 
				new int[]{R.id.game_rsp_cardtable_item_image});
		p1CardTableGV.setAdapter(p1CardAdapter);
		p1CardTableGV.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowID) {
				//HashMap<String, Object> item = (HashMap<String, Object>)parent.getItemAtPosition(position);
				switch(p1CardImages[position]){
				case R.drawable.card_rock:
					chooseRock();
					break;
				case R.drawable.card_scissors:
					chooseScissors();
					break;
				case R.drawable.card_paper:
					choosePaper();
					break;
				}
			}
			
		});

		int[] p2CardImages = new int[]{R.drawable.card_back, R.drawable.card_back, R.drawable.card_back};
		
		
		ArrayList<HashMap<String, Object>> p2Cards = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<p1CardImages.length; i++){
			HashMap<String, Object> card = new HashMap<String, Object>();
			card.put("cardImage", p2CardImages[i]);
			p2Cards.add(card);
		}
		
		SimpleAdapter p2CardAdapter = new SimpleAdapter(this, p2Cards, R.layout.cardtable_item, 
				new String[]{"cardImage"}, 
				new int[]{R.id.game_rsp_cardtable_item_image});
		p2CardTableGV.setAdapter(p2CardAdapter);
		
		
		
		vsMsgLayout.setVisibility(View.GONE);
		roundMsgLayout.setVisibility(View.GONE);
		p1CardTableLayout.setVisibility(View.GONE);
		p2CardTableLayout.setVisibility(View.GONE);
		fightMsgLayout.setVisibility(View.GONE);
		resultMsgLayout.setVisibility(View.GONE);
		p1ChoiceLayout.setVisibility(View.GONE);
		p2ChoiceLayout.setVisibility(View.GONE);
		
		
	}
	
	
	@Override
	protected RSPGame createGameInstance() {
		return new RSPGame(profile.getMaxRound());
	}

	@Override
	protected boolean initGame() {
		
		game.addCustomizedListener(this);
		
		return true;
	}

	@Override
	protected void loadSFX() {
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gameover, SFX_LOSE);
		soundPlayer.load(R.raw.gamefinish, SFX_WIN);
		soundPlayer.load(R.raw.hint, SFX_ROUND1);
		soundPlayer.load(R.raw.hint, SFX_ROUND2);
		soundPlayer.load(R.raw.hint, SFX_ROUND3);
		soundPlayer.load(R.raw.hint, SFX_ROUND4);
		soundPlayer.load(R.raw.hint, SFX_ROUND5);
		soundPlayer.load(R.raw.hint, SFX_ROUNDFINAL);
		soundPlayer.load(R.raw.blockselected, SFX_CHOOSE);
		soundPlayer.load(R.raw.hint, SFX_SHOWRESULT);
	}

	@Override
	protected int getBGMResourceID() {
		return R.raw.music_bg;
	}


	@Override
	protected IGameProfileFactory<RSPProfile> getProfileFactory() {
		return new RSPProfileFactory();
	}

	@Override
	protected int getMenuId() {
		return R.menu.rsp;
	}
	
	@Override
	protected void initConfigItems() {
		configItems.put(CONFIGITEM_LEVEL, ConfigManager.CONFIG_RSP_GAME_LEVEL);
		configItems.put(CONFIGITEM_MUSIC, ConfigManager.CONFIG_RSP_GAME_MUSIC_ON);
		configItems.put(CONFIGITEM_SFX, ConfigManager.CONFIG_RSP_GAME_SFX_ON);
	}

	@Override
	public void onRoundStarted(int roundNumber) {
		roundNumberTV.setText(Integer.toString(roundNumber));
		showRoundMsg();
		
	}

	@Override
	public void onP1Ready() {
		p1CardTableLayout.setVisibility(View.GONE);
		if(game!=null && game.isReadyForFight()){
			showFightingMsg();
		}
	}

	@Override
	public void onP2Ready() {
		if(game!=null && game.isReadyForFight()){
			showFightingMsg();
		}
	}
	
	@Override
	public void onFight() {
		showChoices();
	}
	
	@Override
	public void onRoundFinished(int roundNumber, RSPResult result) {
		if(result == RSPResult.WIN){
			showWinMsg();
		}else if(result == RSPResult.LOSE){
			showLoseMsg();
		}else if(result == RSPResult.DRAW){
			showDrawMsg();
		}
		game.gameChecking();
	}

	@Override
	public void onFinished(RSPGame game) {
		// TODO Auto-generated method stub
		super.onFinished(game);
	}


	@Override
	public void onPaused(RSPGame game) {
		// TODO Auto-generated method stub
		super.onPaused(game);
	}


	@Override
	public void onResumed(RSPGame game) {
		// TODO Auto-generated method stub
		super.onResumed(game);
	}


	@Override
	public void onStarted(RSPGame game) {
		showVsMsg();
		
		super.onStarted(game);
	}


	@Override
	public void onStopped(RSPGame game) {
		// TODO Auto-generated method stub
		super.onStopped(game);
	}


	@Override
	public void onGameOver(RSPGame game) {
		// TODO Auto-generated method stub
		super.onGameOver(game);
	}
	
	private void showVsMsg() {
		vsMsgLayout.setVisibility(View.VISIBLE);
		
		
		Animation ani1 = new TranslateAnimation(Animation.ABSOLUTE, screenWidth, Animation.ABSOLUTE, 0,
	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		
		ani1.setDuration(1500);
		ani1.setFillAfter(true);
		//ani1.setStartOffset(1000);
		ani1.setRepeatCount(0);
	
		p1VSNameTV.setAnimation(ani1);
		
		Animation ani2 = new TranslateAnimation(Animation.ABSOLUTE, -screenWidth, Animation.ABSOLUTE, 0,
	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		
		ani2.setDuration(1500);
		ani2.setFillAfter(true);
		//ani2.setStartOffset(1000);
		ani2.setRepeatCount(0);
		
		
		p2VSNameTV.setAnimation(ani2);
		
		final AnimationSet as = new AnimationSet(true);
		ScaleAnimation ani3 = new ScaleAnimation(1f,3f, 1f, 3f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation ani4 = new AlphaAnimation(1.0f, 0.0f);
		
		as.addAnimation(ani3);
		as.addAnimation(ani4);
		
		as.setDuration(1000);
		as.setFillAfter(true);
		as.setStartOffset(2500);
		
		vsMsgLayout.setAnimation(as);
		
		ani1.start();
		ani2.start();
		as.start();
		
		as.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				vsMsgLayout.setVisibility(View.GONE);
				game.startNewRound();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
	}
	
	private void showRoundMsg() {
		roundMsgLayout.setVisibility(View.VISIBLE);
		
		final AnimationSet as = new AnimationSet(true);
		ScaleAnimation ani1 = new ScaleAnimation(1f,3f, 1f, 3f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation ani2 = new AlphaAnimation(1.0f, 0.0f);
		
		as.addAnimation(ani1);
		as.addAnimation(ani2);
		
		as.setDuration(1000);
		as.setFillAfter(true);
		as.setStartOffset(1500);
		
		roundMsgLayout.setAnimation(as);
	
		as.start();
		
		as.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				roundMsgLayout.setVisibility(View.GONE);
				showCardTable();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
	}
	
	private void showCardTable() {
		p1CardTableLayout.setVisibility(View.VISIBLE);
		p2CardTableLayout.setVisibility(View.VISIBLE);
		
		Animation ani1 = new TranslateAnimation(Animation.ABSOLUTE, screenWidth, Animation.ABSOLUTE, 0,
	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		
		ani1.setDuration(1500);
		ani1.setFillAfter(true);
		//ani1.setStartOffset(1000);
		ani1.setRepeatCount(0);
	
		
		
		p1CardTableLayout.setAnimation(ani1);
		
		Animation ani2 = new TranslateAnimation(Animation.ABSOLUTE, -screenWidth, Animation.ABSOLUTE, 0,
	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		
		ani2.setDuration(1500);
		ani2.setFillAfter(true);
		//ani2.setStartOffset(1000);
		ani2.setRepeatCount(0);
		ani2.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				computerChoose();
			}

			

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
		
		p2CardTableLayout.setAnimation(ani2);
		
		
		ani1.start();
		ani2.start();
	}
	
	private void chooseRock() {
		if(game!=null){
			game.setP1Choice(RSPType.ROCK);
		}
	}
	
	private void chooseScissors() {
		if(game!=null){
			game.setP1Choice(RSPType.SCISSORS);
		}
	}

	private void choosePaper() {
		if(game!=null){
			game.setP1Choice(RSPType.PAPER);
		}
	}
	
	private void computerChoose() {
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		RSPType choice = null;
		int choose = r.nextInt(3);
		switch(choose){
		case 0:
			choice = RSPType.ROCK;
			break;
		case 1:
			choice = RSPType.SCISSORS;
			break;
		case 2:
			choice = RSPType.PAPER;
			break;
		}
		if(game!=null){
			game.setP2Choice(choice);
		}
	}

	private void showFightingMsg() {
		
		p2CardTableLayout.setVisibility(View.GONE);
		
		fightMsgLayout.setVisibility(View.VISIBLE);
		
		final AnimationSet as = new AnimationSet(true);
		ScaleAnimation ani1 = new ScaleAnimation(1f,3f, 1f, 3f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation ani2 = new AlphaAnimation(1.0f, 0.0f);
		
		as.addAnimation(ani1);
		as.addAnimation(ani2);
		
		as.setDuration(1000);
		as.setFillAfter(true);
		as.setStartOffset(1500);
		
		fightMsgLayout.setAnimation(as);
	
		as.start();
		
		as.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				fightMsgLayout.setVisibility(View.GONE);
				game.fight();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
	}
	private void showWinMsg() {
		showResultMsg(R.drawable.result_win_lable);
	}
	
	private void showLoseMsg() {
		showResultMsg(R.drawable.result_lose_lable);
	}
	
	private void showDrawMsg() {
		showResultMsg(R.drawable.result_draw_lable);
	}
	
	private void showResultMsg(int resultResId) {
		resultMsgLayout.setVisibility(View.VISIBLE);
		resultMsgIV.setBackgroundResource(resultResId);
		
		final AnimationSet as = new AnimationSet(true);
		ScaleAnimation ani1 = new ScaleAnimation(1f,3f, 1f, 3f, ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation ani2 = new AlphaAnimation(1.0f, 0.0f);
		
		as.addAnimation(ani1);
		as.addAnimation(ani2);
		
		as.setDuration(1000);
		as.setFillAfter(true);
		as.setStartOffset(1500);
		
		resultMsgLayout.setAnimation(as);
	
		as.start();
		
		as.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
	}
	
	private void showChoices() {
		p1ChoiceLayout.setVisibility(View.VISIBLE);
		p2ChoiceLayout.setVisibility(View.VISIBLE);
		
		RSPType p1Choice = game.getCurrentRound().getP1Choice();
		RSPType p2Choice = game.getCurrentRound().getP2Choice();
		
		
		if(p1Choice ==  RSPType.ROCK){
			p1ChoiceIV.setBackgroundResource(R.drawable.card_rock);
		}else if(p1Choice ==  RSPType.SCISSORS){
			p1ChoiceIV.setBackgroundResource(R.drawable.card_scissors);
		}else if(p1Choice ==  RSPType.PAPER){
			p1ChoiceIV.setBackgroundResource(R.drawable.card_paper);
		}
		
		if(p2Choice ==  RSPType.ROCK){
			p2ChoiceIV.setBackgroundResource(R.drawable.card_rock);
		}else if(p2Choice ==  RSPType.SCISSORS){
			p2ChoiceIV.setBackgroundResource(R.drawable.card_scissors);
		}else if(p2Choice ==  RSPType.PAPER){
			p2ChoiceIV.setBackgroundResource(R.drawable.card_paper);
		}
		
		Animation ani1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f,
	            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		
		ani1.setDuration(1500);
		ani1.setFillAfter(true);
		ani1.setStartOffset(1000);
		ani1.setRepeatCount(0);
	
		p1ChoiceLayout.setAnimation(ani1);
		
		Animation ani2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	            Animation.ABSOLUTE, screenHeight, Animation.ABSOLUTE, screenHeight/2);
		
		ani2.setDuration(1500);
		ani2.setFillAfter(true);
		ani2.setStartOffset(1000);
		ani2.setRepeatCount(0);
		ani2.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				game.finishRound();
			}

			

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
		});
		
		p2ChoiceLayout.setAnimation(ani2);
		
		
		ani1.start();
		ani2.start();
	}
	
}
