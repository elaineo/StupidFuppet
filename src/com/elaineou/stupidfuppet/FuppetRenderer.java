package com.elaineou.stupidfuppet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

public class FuppetRenderer implements GLSurfaceView.Renderer 
{
	private static final String TAG = "Fuppet"; 
	public volatile float mAngle;

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	
	/** Store our model data in a float buffer. */
	private final FloatBuffer mTriangle3Vertices;
	private final FloatBuffer mEyeVertices;
	private final FloatBuffer mFaceVertices;
	private final FloatBuffer mBallVertices;
	private final FloatBuffer mMouthVertices;
	private final ShortBuffer drawListBuffer;

	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;
	
	/** This will be used to pass in model position information. */
	private int mPositionHandle;
	
	/** This will be used to pass in model color information. */
	private int mColorHandle;
	
	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;
	
	/** How many elements per vertex. */
	private final int mStrideBytes = 7 * mBytesPerFloat;	
	
	/** Offset of the position data. */
	private final int mPositionOffset = 0;
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;
	
	/** Offset of the color data. */
	private final int mColorOffset = 3;
	
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;		
		
	/** order of circle coords **/
    private final short drawOrder[] = { 0, 9,10,0,10,11,0,11,12,0,12,13,
										0,13,14,0,14,15,0,15,16,0,16, 1,
										0, 1, 2,0, 2, 3,0, 3, 4,0, 4, 5,0, 5, 6,0, 6, 7,
										0, 7, 8,0, 8, 9}; 

	/**
	 * Initialize the model data.
	 */
	public FuppetRenderer()
	{	
	    
		// eyeball vertices
	    final float[] circleCoords = { 
	    		0.0f, 0.0f, 0.0f,
	    		1.0f, 1.0f, 1.0f, 1.0f,
			-1.0f,  0.0f, 0.0f,   
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.866f, 0.5f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.707f, 0.707f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, 0.866f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.866f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.707f, 0.707f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.866f, 0.5f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.866f, -0.5f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.707f, -0.707f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.5f, -0.866f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			0.0f, -1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, -0.866f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.707f, -0.707f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
			-0.866f, -0.5f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
			};
	    float[] faceCoords = new float[circleCoords.length];
	    float[] eyeBallCoords = new float[circleCoords.length];
	    float[] mouthCoords = new float[circleCoords.length];
	    System.arraycopy( circleCoords, 0, faceCoords, 0, circleCoords.length );
	    System.arraycopy( circleCoords, 0, eyeBallCoords, 0, circleCoords.length );
	    System.arraycopy( circleCoords, 0, mouthCoords, 0, circleCoords.length );
	    final float[] ballcolor = {1.0f, 1.0f, 1.0f, 1.0f};
	    final float[] facecolor = {0.0f, 1.0f, 0.0f, 1.0f};
	    final float[] mouthcolor = {0.8f, 0.0f, 0.0f, 1.0f};
	    /* colored people */	    
	    for (int i=7;i<circleCoords.length;i++) {
	    	int j = i%7;
	    	if (j>2) {
    			faceCoords[i]=facecolor[j-3];
    			eyeBallCoords[i]=ballcolor[j-3];
    			mouthCoords[i]=mouthcolor[j-3];
	    	}
	    }
				
		// This triangle is white, gray, and black.
		final float[] triangle3VerticesData = {
				// X, Y, Z, 
				// R, G, B, A
	            -0.5f, -0.25f, 0.0f, 
	            0.0f, 0.0f, 0.0f, 1.0f,	            
	            0.5f, -0.25f, 0.0f, 
	            0.0f, 0.0f, 0.0f, 1.0f,	            
	            0.0f, 0.559016994f, 0.0f, 
	            0.0f, 0.0f, 0.0f, 1.0f};

		
		// Initialize the buffers.
		mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();					
		mTriangle3Vertices.put(triangle3VerticesData).position(0);
		

	    ByteBuffer bb = ByteBuffer.allocateDirect(
    			circleCoords.length * mBytesPerFloat).order(ByteOrder.nativeOrder());
	    mEyeVertices = bb.asFloatBuffer();
	    mEyeVertices.put(circleCoords).position(0);

		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(
			// (# of coordinate values * 2 bytes per short)
			drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder).position(0);

	    ByteBuffer bbface = ByteBuffer.allocateDirect(
    			faceCoords.length * mBytesPerFloat).order(ByteOrder.nativeOrder());
	    mFaceVertices = bbface.asFloatBuffer();
	    mFaceVertices.put(faceCoords).position(0);		
	    
	    ByteBuffer bball = ByteBuffer.allocateDirect(
    			eyeBallCoords.length * mBytesPerFloat).order(ByteOrder.nativeOrder());
	    mBallVertices = bball.asFloatBuffer();
	    mBallVertices.put(eyeBallCoords).position(0);	
	    
	    ByteBuffer bbmouth = ByteBuffer.allocateDirect(
    			mouthCoords.length * mBytesPerFloat).order(ByteOrder.nativeOrder());
	    mMouthVertices = bbmouth.asFloatBuffer();
	    mMouthVertices.put(mouthCoords).position(0);	
		
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
	
		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		final String vertexShader =
			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
			
		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.			  
		  
		  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
		  
		  + "void main()                    \n"		// The entry point for our vertex shader.
		  + "{                              \n"
		  + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader. 
		  											// It will be interpolated across the triangle.
		  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
		  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
		  + "}                              \n";    // normalized screen coordinates.
		
		final String fragmentShader =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
													// precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
		  											// triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}                              \n";												
		
		// Load in the vertex shader.
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);

			// Compile the shader.
			GLES20.glCompileShader(vertexShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0)
		{
			throw new RuntimeException("Error creating vertex shader.");
		}
		
		// Load in the fragment shader shader.
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

			// Compile the shader.
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0)
		{
			throw new RuntimeException("Error creating fragment shader.");
		}
		
		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
        
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        
        
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);       
        
	}	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}	
	
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        
                
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        //face 
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix,0,1.3f,0.9f,1.5f);
        drawCircle(mFaceVertices);
        
        //eye balls 
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.5f, 0.3f, 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.2f,0.2f,0);
        drawCircle(mBallVertices);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -0.5f, 0.3f, 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.2f,0.2f,0);
        drawCircle(mBallVertices);
        
        // Right eye
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.5f, 0.3f, 0.0f);
        Matrix.translateM(mModelMatrix, 0, 0.1f*(float) Math.sin(mAngle), 0.1f*(float) Math.cos(mAngle), 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.05f,0.05f,0);
        drawCircle(mEyeVertices);
        
        // Left eye
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -0.5f, 0.3f, 0.0f);
        Matrix.translateM(mModelMatrix, 0, 0.1f*(float) Math.sin(mAngle), 0.1f*(float) Math.cos(mAngle), 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.05f,0.05f,0);
        drawCircle(mEyeVertices);
        
        //mouth 
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -0.3f, 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.6f,0.3f,0);
        drawFracCircle(mMouthVertices);
	}	
	
	/**
	 * Draws a triangle from the given vertex data.
	 * 
	 * @param aTriangleBuffer The buffer containing the vertex data.
	 */
	private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{		
		// Pass in the position information
		aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);


        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);                               
	}
	
	private void drawCircle(final FloatBuffer aCircleBuffer)
	{		
		//float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
		// Pass in the position information
		aCircleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aCircleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Set color for drawing 
        // Pass in the color information
        aCircleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aCircleBuffer);    
        
        GLES20.glEnableVertexAttribArray(mColorHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);                               
	
        // Draw the circle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);	
	}
	
	private void drawFracCircle(final FloatBuffer aCircleBuffer)
	{		
		//float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
		// Pass in the position information
		aCircleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aCircleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Set color for drawing 
        // Pass in the color information
        aCircleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aCircleBuffer);    
        
        GLES20.glEnableVertexAttribArray(mColorHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
	
        // Draw the circle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length/2,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);	
	}

}