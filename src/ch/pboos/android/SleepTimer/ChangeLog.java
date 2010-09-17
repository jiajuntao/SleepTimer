package ch.pboos.android.SleepTimer;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class ChangeLog extends Activity {

	TextView changelog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExceptionHandler.register(this, "http://pboos.ch/bugs/server.php");
		
		setContentView(R.layout.changelog);
		
		changelog = (TextView)findViewById(R.id.textview_changelog);
		String strChange = "" +
		"<h1>ChangeLog</h1>" +
		"<h2>0.10.1</h2>" +
		"- Background service improved<br/>" +
		"- Added Settings: Send pause/stop media broadcast<br/>" +
		"- Added player: Zimly Media Player (thx to Tony T.)<br/>" +
		"- Added player: Androrb (thx to Simon O.)<br/>" +
		"- Added player: Player Pro (thx to John E.)<br/>" +
		"- Added player: Arc media (thx to Anthony L.)<br/>" +
		"- Added player: Energy Radio (thx to Benjamin L.)<br/>" +
		
		"<h2>0.9.16</h2>" +
		"- Added player: RockPlayer (thx to Bryan B.)<br/>" +
		"- Added player: r2player (thx to Jae K.)<br/>" +
		"- Added player: MLB At Bat 2010 (thx to Jae K.)<br/>" +
		"- Updated player: MixZing (should now work again)<br/>" +
		"- BugFix: A force close has been fixed on some rooted devices<br/>" +
		
		"<h2>0.9.15</h2>" +
		"- Added player: Mecanto<br/>" +
		"- Added player: Music online<br/>" +
		"- Added player: Kkbox<br/>" +
		"- Added player: Samsung Music Player (did not always work. Hope now okay)<br/>" +
		"- BugFix: Players starting again. (please give me feedback if it works better now)<br/>" +
		"- BugFix: Infobox on unrooted devices showing always<br/>" +
		"- Personal: Got married on the 21st of August. Donations welcome! :)<br/>" +
		
		"<h2>0.9.14</h2>" +
		"- BugFix: Default player starts (please give me feedback if it works better now)<br/>" +
		
		"<h2>0.9.13</h2>" +
		"- Support for unrooted Froyo 2.2 (only some Apps will be stopped)<br/>" +
		"-- Emulate Media Pause Key press to stop players. This will allow SleepTimer to work on unrooted 2.2 for applications which support that event. KeyEvent up and down: KEYCODE_MEDIA_PLAY_PAUSE (thx to Daniel V.)<br/>" +
		"- BugFix: Force close on rooted devices<br/>" +
		"- Added support for Samsung Vibrant (thx to Michael C.)<br/>" +
		"- Added support for NPR Paid (thx to Alvin S.)<br/>" +
		"- Added support for Androradio<br/>" +
		"- Added support for HTC FM Radio<br/>" +
		"- Fixed support for last.fm, acast, iheartradio, pandora, ...<br/>" +
		
		"<h2>0.9.12</h2>" +
		"- Bugfix: Shake extend not working (will work on an improved version of this soon)<br/>" +
		
		"<h2>0.9.11</h2>" +
		"- Bugfix: settings not saved (thx to Gordon B. and others)<br/>" +
		
		"<h2>0.9.10</h2>" +
		"- Froyo working (only with root)<br/>" +
		"- Bluetooth fixed (please report if working)<br/>" +
		"- Added support for Samsung Music Player (thx to Nathan and Yannick)<br/>" +

		"<h2>0.9.9</h2>" +
		"- Workaround for Android Froyo<br/>" +
		"- Added support for Samsung Music Player (thx to Dyllon B.)<br/>" +
		"- Added support for Spotify (thx to Matt O.)<br/>" +
		"- Added support for ACast (thx to Katie R.)<br/>" +
		"- Added support for dPod (thx to Mandie W.)<br/>" +
		
		"<h2>0.9.8</h2>" +
		"- Added support for 3 - Cubed (thx to ijames99 and &quot;me&quot;)<br/>" +		
				
		"<h2>0.9.7</h2>" +
		"- Added support for bTunes (thx to Stan H. and James)<br/>" +		
		
		"<h2>0.9.6</h2>" +
		"- Added support for Rhapsody (thx to Ardis-Mary W.)<br/>" +
		
		"<h2>0.9.5</h2>" +
		"- Changed some small things in the user interface for better understanding.<br/>" +
		"- Removed \"Stop own service\"<br/>" +
		"- Added possibility to stop any program<br/>" +
		"- Added support for Radiotime (thx to Steve B.)<br/>" +
		"- Added support for NRK RADIO (thx to Morten H.)<br/>" +
		"- Added support for Grooveshark (thx to NTulip)<br/>" +
		
		"<h2>0.9.4</h2>" +
		"- Added support for MortPlayer (thx to John M.)<br/>" +
		"- Added support for MaplePlayer (thx to John M.)<br/>" +
		"- Added support for Astro Player (thx to Dan)<br/>" +
		
		"<h2>0.9.3</h2>" +
		"- Added support for Stitcher (thx to Linda F.)<br/>" +
				
		"<h2>0.9.2</h2>" +
		"- Added a little buggy support for Amblingbooks BookPlayer (thx to Howard & Russ)<br/>" +
		"- Added support for Crossforward Audiobooks (thx to Chris C.)<br/>" +
		"- Added support for iheartradio (thx to Michael R.)<br/>" +
		"- Added support for musiconlinelite (thx to Leticia M.)<br/>" +
		"- AdMob Advertisements (Sorry folks, but at least the app stays free! :) )<br/>" +
		
		"<h2>0.9.1</h2>" +
		"- Better automatic bug report on in program error.<br/>" +
		
		"<h2>0.9</h2>" +
		"- Turn off Bluetooth (thx to Jim T.)<br/>" +
		"- Go into airplane mode (thx to many)<br/>" +
		"- Updated user interface<br/>" +
		
		"<h2>0.8.3</h2>" +
		"- Added support for last.fm (new version) (thx to Fabian M.)<br/>" +
		"- Added support for DroidLiveLite (thx to Angelo F.)<br/>" +
		"- Added support for A Online Radio (thx to Zlatan)<br/>" +
		
		"<h2>0.8.2</h2>" +
		"- Added support for Slacker Radio (thx to Don)<br/>" +
		"- Added support for DroidLive (thx to Mark M.)<br/>" +
		
		"<h2>0.8.1</h2>" +
		"- Change in Settings to not let it load so long. But still loads long for ApplicationList.<br/>" +
		
		"<h2>0.8</h2>" +
		"- Added Shake extend. Shake to run longer again. Possible in the last minute after a notification. (thx to Christian Danzmann)<br/>" +
		"- Added support for Carcast player (thx to Ulvestad)<br/>" +
		"- Fixed a force close bug when starting android music player<br/>" +
		
		"<h2>0.7.1</h2>" +
		"- Added support for MixZing player (thx to gal)<br/>" +
		
		"<h2>0.7</h2>" +
		"- Added button to start Music player (thx to Christopher)<br/>" +
		"- Added settings<br/>" +
		"-- Turn off WiFi as option<br/>" +
		"-- Mute notifications (thx to dunn)<br/>" +
		"-- Set music player<br/>" +
		
		"<h2>0.6</h2>" +
		"- Added support for nswplayer (thx to farraguas)<br/>" +
		"- New way to choose the amount of minutes<br/>" +
		"- Set amount of minutes will be remembered<br/>" +
		"- Fixed: Bug with Hero lock screen<br/>" +
		
		"<h2>0.5.1</h2>" +
		"- Fixed: force close bug<br/>" +
		
		"<h2>0.5</h2>" +
		"- Small changes to interface<br/>" +
		"- Automatic sending bug report of force closes<br/>" +
		
		"<h2>0.4</h2>" +
		"- Improved Feedback (include running services)<br/>" +
		"- Choose Service to kill (like unsupported player)<br/>" +
		"- New players supported:<br/>" +
		"-- Google Listen support<br/>" +
		"-- TuneWiki support<br/>" +
		"-- Imeem support<br/>" +
		"-- Meridian support<br/>" +
		"-- Last.fm support<br/>" +
		"-- RockOn Lite support<br/>" +
		"-- Pandora support (untested)<br/>" +
		"-- Beyondpod support (untested)<br/>" +
		"-- Mediafly support (untested)<br/>" +
		"-- Doggcatcher support (untested)<br/>" +
		"-- Mixzing support (untested)<br/>" +
		"-- Droidlive support (untested)<br/>" +
		
		"<h2>0.3</h2>" +
		"- HTC Music Player support<br/>" +
		"- Streamfurious Lite support (and plus?)<br/>" +
		"- Feedback (bugs/requests) through e-mail<br/>" +
		
		"<h2>0.2</h2>" +
		"- Updated User Interface<br/>" +
		"- Added changelog<br/>" +
		"- Internal improvement in source code<br/>" +
		
		"<h2>0.1</h2>" +
		"- Stopping player<br/>" +
		"- Fade out<br/>" +
		"- Only standard android music player<br/>" +
				"";
		Spanned test = Html.fromHtml(strChange);
		changelog.setText(test);
	}
}
