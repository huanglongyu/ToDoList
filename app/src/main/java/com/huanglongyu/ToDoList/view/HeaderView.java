package com.huanglongyu.ToDoList.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huanglongyu.ToDoList.R;

public class HeaderView extends LinearLayout implements View.OnTouchListener {
    private static final String TAG = "HeaderView";
    private LinearLayout mContainer;
    private EditText mEditText;
    private int showHeight;
    private int maxHeight = 0;
    private float percentage;
    private float currentAngle;
    private Context mContext;
    private InputMethodManager imm;

    private Bitmap viewBitmap;
    private Camera mCamera;
    private Matrix mMatrix;
    private Paint mPaint;
    private boolean isOverridDispatch = true;
    private OnHeaderTriggerListener mOnHeaderTriggerListener;

    public HeaderView(Context context) {
        super(context);
        initView(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HeaderView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public interface OnHeaderTriggerListener{
        void onNewTaskCancelTriggered();
        void onNewTaskAddedTriggerd(String item);
    }

    public void setOnHeaderTriggerListener(OnHeaderTriggerListener listener){
        mOnHeaderTriggerListener = listener;
    }

    public int getVisiableHeight(){
        return getHeight();
    }

    public String getEditTextContent(){
        return mEditText.getText().toString();
    }

    private void initView(Context context) {
        mContext = context;
        imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        setOnTouchListener(this);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.todolistview_headerview, null);
        mEditText = (EditText)mContainer.findViewById(R.id.head_new_task);
        mEditText.setOnTouchListener(this);
        mContainer.setOnTouchListener(this);
//        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.todo_list_item, null);
//        mEditText = (EditText)mContainer.findViewById(R.id.item_info);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        mEditText.setSingleLine(true);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                            KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEND
                                || actionId == EditorInfo.IME_ACTION_DONE) {
                                //|| (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {

                            Log.i(TAG, "actionId:"+ actionId + " " + (event != null ? event.getKeyCode() : -100) + " "
                                    + (event != null ? event.getAction() : -200));

                           if (v.getText().toString().trim().equals("") || v.getText().toString().trim().length() ==0) {
                               imm.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
                               if(mOnHeaderTriggerListener != null){
                                   mOnHeaderTriggerListener.onNewTaskCancelTriggered();
                               }
                           } else {
                               Log.i(TAG, "add new item");
                               if(mOnHeaderTriggerListener != null){
                                   mOnHeaderTriggerListener.onNewTaskAddedTriggerd(v.getText().toString().trim());
                               }
                           }
                        }
                        return false;
                    }
                });
        mEditText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
//                Logger.i("onTextChanged");
            }
 
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
 
            @Override
            public void afterTextChanged(Editable s) {
               
            }
        });
//        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
//        Logger.i("longyu","-----getWidth:" + getWidth());
////        LayoutParams lp = new LayoutParams(this.getWidth(), 0);
//        lp.gravity = Gravity.CENTER;
//        addView(mContainer, lp);
//        initHeight();
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (isOverridDispatch && viewBitmap != null && viewBitmap.getHeight() > 0 && viewBitmap.getHeight() > 0) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
//            super.dispatchDraw(new Canvas(bitmap));
//            super.dispatchDraw(canvas);
//            rotateAngle = (int) (57.295779513082323D * Math
//                    .acos((double) ((int) ((float) maxHeight - delta) >> 2) / (maxHeight/(double) 4)));
//            Logger.i("canvasHeight:" + canvasHeight + " " + viewBitmap.getHeight() + " getVisiableHeight:" + getVisiableHeight());// + " drawBitmap:" + drawBitmap.getHeight());
//            Rect r = new Rect();
//            getGlobalVisibleRect(r);
//            Logger.i("dispatchDraw!!!!!!!!height:" + canvasHeight + " r:" + r);
            float dalt = 90 * percentage;
//          drawBitmap = newBitmapMy(viewBitmap,rotateAngle);
            final Camera camera = mCamera;
            final Matrix matrix = mMatrix;
            rotate(viewBitmap,canvas.getWidth() >> 1, canvas.getHeight() >> 1,camera,matrix,270 + dalt);
            drawBitmap(viewBitmap,canvas, canvas.getWidth() >> 1, canvas.getHeight() >> 1, 0.0f, matrix);
//            canvas.drawBitmap(tmpBit, 0, 0, mPaint);
//            Logger.i("longyu", "canvasWidth:" + canvasWidth + " canvasHeight:" + canvasHeight +
//                    " viewBitmapH :" + viewBitmap.getHeight() + " dalt:" + dalt);
        }else{
            super.dispatchDraw(canvas);
//            Logger.i("super.dispatchDraw(canvas) viewBitmap:" + viewBitmap);
        }
    }

//    private Bitmap rotate(Bitmap bitmap, int pRotateX, int pRotateY, int pRotateZ) {
//        mCamera.save();
//
//        mCamera.rotateX(pRotateX);
//        mCamera.rotateY(pRotateY);
//        mCamera.rotateZ(pRotateZ);
//        Matrix matrix = new Matrix();
//        mCamera.getMatrix(matrix);
//        matrix.preTranslate(bitmap.getWidth() >> 1, bitmap.getHeight() >> 1);
//        mCamera.restore();
//        
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
//        return newBitmap;
//    }

    private void drawBitmap(Bitmap map,Canvas canvas, float centerX, float centerY, float offset,
            Matrix matrix) {
//        canvas.save();
//        canvas.translate(offset, 0.0f);
//        float a[] = new float[9];
//        matrix.getValues(a);
//        Logger.i("longyu","matrix:" + printM(a));
//        canvas.concat(matrix);
//        matrix.postRotate(290);
//        matrix = new Matrix();
//        matrix.preRotate(200);
        canvas.drawBitmap(map, matrix, mPaint);
//        canvas.drawBitmap(map, 0, 0, mPaint);
//        canvas.drawBitmap(viewBitmap, centerX, centerY, mPaint);
//        canvas.restore();
    }

    private String printM(float[] a){
        StringBuffer sb = new StringBuffer();
        for(int i=0 ;i<a.length;i++){
            sb = sb.append(a[i] + ",");
        }
        return sb.toString();
    }

    private void rotate(Bitmap bitmap, float centerX, float centerY,
            Camera camera, Matrix matrix, float angle) {
        camera.save();
        camera.translate(0.0f, 0.0f, 100 * (1.0f - percentage));
        camera.rotateX(angle);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-bitmap.getWidth() >> 1, -bitmap.getHeight() >> 1);
        matrix.postTranslate(bitmap.getWidth() >> 1, bitmap.getHeight() >> 1);
    }


//    private void initHeight(){
//        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int height = mContainer.getHeight();
//                Logger.i("longyu","mContainer height :" + height);
//                getViewTreeObserver().removeGlobalOnLayoutListener(this);
//            }
//        });
//    }
//    public void doNewTaskAnimation(){
//        while(showHeight < maxHeight){
//            int a = showHeight + 1;
//            setShowHeight(a);
//        }
//    }

//    public void doRemoveTaskAnimation(){
//        while(showHeight > 0){
//            int a = showHeight -1;
//            setShowHeight(a);
//        }
//    }

    public void setShowHeight(int newHeight){
//        if(newHeight == showHeight){
//            Logger.i("setShowHeight not change return, newHeight:" + newHeight);
//            return;
//        }
        showHeight = newHeight;
        if(showHeight > maxHeight && maxHeight != 0){
            showHeight = maxHeight;
        }
//        Logger.i("set new height :" + showHeight + " percentage:" + percentage);
        if(showHeight < 0){
//            Logger.i("showHeight < 0," + showHeight + " height:" + getHeight());
            showHeight = 0;
        }
        percentage = (float)showHeight/maxHeight;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = showHeight;
        mContainer.setLayoutParams(lp);
        setBottom(showHeight);
        setTop(0);
//        if(showHeight == 0){
//            Logger.i("trigger up");
//        }else if(showHeight == maxHeight){
//            Logger.i("trigger down");
//        }
        mContainer.setDrawingCacheEnabled(true);
        mContainer.buildDrawingCache();
        viewBitmap = mContainer.getDrawingCache();
        invalidate();
    }

    public void setMaxHeight(int max){
        maxHeight = max;
    }

    public void setFocus(boolean focus){
        if(focus){
            isOverridDispatch = false;
            mEditText.requestFocus();
            imm.showSoftInput(mEditText, 0);
        }else{
            isOverridDispatch = true;
            mEditText.clearFocus();
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(),0);  
        }
    }

    public void initHead(){
      DisplayMetrics dm = getResources().getDisplayMetrics();
      int w_screen = dm.widthPixels;
      int pendding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
      LayoutParams lp = new LayoutParams(w_screen - pendding * 2, 0);
      lp.gravity = Gravity.CENTER;
      addView(mContainer, lp);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view instanceof EditText){
            ((ViewGroup)view.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }else if(view instanceof ViewGroup){
            ((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        return false;
    }

//    public void setVisiableHeight(int height){
//        if (height < 0)
//            height = 0;
//        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
//        lp.height = height;
//        mContainer.setLayoutParams(lp);
//    }
    
}
