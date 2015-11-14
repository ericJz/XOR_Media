package com.zw.player.playerserver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class SeekbarView extends View {  
    
    private float maxProgress;  
    private float progress = 0;  
    private float playprg = 0;
    private int progressStrokeWidth = 6;  
    private String processText="";
    private String duration="";
    // 画圆所在的距形区域  
    Paint paint,paintText;  
    RectF rect1,rect2,rect3,rect4;
    public int color1 = 0x00cccccc;
    public int color2 = 0x33bfbfbf;
    public int color3 = 0x00cccccc;
    public int color4 = 0x00cccccc;
  
    public SeekbarView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub    
        paint = new Paint();  
        paintText = new Paint();
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
        // TODO Auto-generated method stub    
        int height = this.getHeight() / 2;    
        
        float startX = this.getWidth() / 6;
        float stopX = this.getWidth() - startX;
        float stopY = height+3;
        float startY = height-3;
        maxProgress = stopX - startX;
        
        rect1 = new RectF(startX,startY,stopX,stopY);
        rect2 = new RectF(startX,startY,(float)(progress*maxProgress)+startX,stopY);
        rect3 = new RectF(startX,startY,(float)(playprg*maxProgress)+startX,stopY);
        rect4 = new RectF(startX,startY,stopX,stopY);
        
        paint.setAntiAlias(true); // 设置画笔为抗锯齿  
        paint.setColor(Color.WHITE); // 设置画笔颜色  
        canvas.drawColor(Color.TRANSPARENT); // 白色背景  
        paint.setStrokeWidth(progressStrokeWidth); // 线宽  
        paint.setStyle(Style.STROKE);  
        
        paintText.setStrokeWidth(2);
        paintText.setColor(Color.WHITE);
        paintText.setAntiAlias(true);
        paintText.setTypeface(Typeface.DEFAULT_BOLD);
        
        canvas.drawText(processText, 15, stopY, paintText);
        paint.setColor(color1);
        canvas.drawRoundRect(rect1,3,10,paint);
        paint.setColor(color2);  
        canvas.drawRoundRect(rect2,3,10,paint);
        paint.setColor(color3);
        canvas.drawRoundRect(rect3, 3, 10, paint);
        canvas.drawText(duration,stopX+10 , stopY, paintText);
        super.onDraw(canvas); 
    }  
  
    public float getMaxProgress() {  
        return maxProgress;  
    }  
  
    public void setMaxProgress(float maxProgress) {  
        this.maxProgress = maxProgress;  
    }  
  
    public void setProgress(float progress) {  
        this.progress = progress;  
        this.invalidate();  
    } 
    
    public void setPlayProgress(float pprogress){
    	this.playprg = pprogress;
        this.invalidate();      	
    }
    
    public static String secToTime(int time) {  
        String timeStr = null;  
        int hour = 0;  
        int minute = 0;  
        int second = 0;  
        if (time <= 0)  
            return "00:00";  
        else {  
            minute = time / 60;  
            if (minute < 60) {  
                second = time % 60;  
                timeStr = unitFormat(minute) + ":" + unitFormat(second);  
            } else {  
                hour = minute / 60;  
                if (hour > 99)  
                    return "99:59:59";  
                minute = minute % 60;  
                second = time - hour * 3600 - minute * 60;  
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
            }  
        }  
        return timeStr;  
    }  
    
    public void setColors(int c1, int c2){
    	color1 = c1;
    	color2 = c2;
    }    
    
    public void setPlayColor(int c3,int c4){
    	color3 = c3;
    	color4 = c4;
    }
    
    public static String unitFormat(int i) {  
        String retStr = null;  
        if (i >= 0 && i < 10)  
            retStr = "0" + Integer.toString(i);  
        else  
            retStr = "" + i;  
        return retStr;  
    } 
  
    /** 
     * 非ＵＩ线程调用 
     */  
    public void setProgressNotInUiThread(float pprogress, float t1,float t2) {  
        //this.progress = progress;  
    	setPlayProgress(pprogress);
    	String text1 = secToTime((int)(t1/1000));
        String text2 = secToTime((int)(t2/1000));
        this.processText = text1;
        this.duration = text2;
        this.postInvalidate();  
    }  
    public void setProgressNotInUiThread(float progress, int num) {  
        //this.progress = progress;  
    	setProgress(progress);
    	this.processText = num+"";
    	this.duration = "100";
        this.postInvalidate();  
    }  
  
}  