package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class AppAssetManager extends ArrayList<String>
{
	Context context;
	ImageView image;
	String path = "";
	AssetManager assetManager;
	StatusBar stt;
	
	public AppAssetManager(Context context) {
		super();
		init(context, null, null);
	}
	
	public AppAssetManager(Context context, String path) {
		super();
		init(context, path, null);
	}
	
	public AppAssetManager(Context context, ImageView image) {
		super();
		init(context, null, image);
	}
	
	public AppAssetManager(Context context, String path, ImageView image) {
		super();
		init(context, path, image);
	}
	
	private void init(Context context, String path, ImageView image) {
		this.context = context;
		if (path != null) this.path = path;
		this.image = image;
		assetManager = context.getAssets();
		stt = new StatusBar("AppAssetManager");
		list();
	}
	
	public AppAssetManager setPath(String path)
	{
		if (path != null) this.path = path;
		list();
		return this;
	}
	
	public boolean setImageFile(String fileName) {
		return setImageFile(fileName, null);
	}
	
	public boolean setImageFile(String fileName, int imageViewID) {
		boolean isOk = false;
		try
		{
			String imageDir = "images/";
			if (!"".equals(path) && path != null) imageDir = path;
			InputStream is = assetManager.open(imageDir + fileName);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			is.close();
			View image = ((Activity) context).findViewById(imageViewID);
			isOk = image instanceof ImageView;
			if (isOk) ((ImageView) image).setImageBitmap(bitmap);
			else stt.error("imageViewID not an ImageView.");
		}
		catch (IOException e)
		{stt.error(e.getMessage());}
		return isOk;
	}
	
	public boolean setImageFile(String fileName, ImageView image) {
		boolean isOk = false;
		try
		{
			String imageDir = "images/";
			if (!"".equals(path) && path != null) imageDir = path;
			InputStream is = assetManager.open(imageDir + fileName);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			is.close();
			if (image == null) image = this.image;
			if (image != null) image.setImageBitmap(bitmap);
			else {
				stt.error("No ImageView to set.");
				return isOk;
			}
			isOk = true;
		}
		catch (IOException e)
		{stt.error(e.getMessage());}
		return isOk;
	}
	
	public String readFileContent(String fileName) {
		StringBuilder builder = new StringBuilder();
		try
		{
			InputStream is = assetManager.open(path + fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		}
		catch (Exception e)
		{stt.error(e.getMessage());}
		return builder.toString();
	}
	
	public String[] list() {
		return list(path);
	}
	
	public String[] list(String path) {
		clear();
		String[] files = null;
		try {
			files = assetManager.list(path);
			if (files != null) {
				for (String file : files) {
					add(file);
				}
			}
		} catch (Exception e) {
			stt.error(e.getMessage());
		}
		return files;
	}
	
	@Override
	public static <E extends Object> List<E> of()
	{
		return ArrayList.of();
	}

	@Override
	public static <E extends Object> List<E> of(E e1)
	{
		return ArrayList.of(e1);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2)
	{
		return ArrayList.of(e1, e2);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3)
	{
		return ArrayList.of(e1, e2, e3);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4)
	{
		return ArrayList.of(e1, e2, e3, e4);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5)
	{
		return ArrayList.of(e1, e2, e3, e4, e5);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6)
	{
		return ArrayList.of(e1, e2, e3, e4, e5, e6);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7)
	{
		return ArrayList.of(e1, e2, e3, e4, e5, e6, e7);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8)
	{
		return ArrayList.of(e1, e2, e3, e4, e5, e6, e7, e8);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9)
	{
		return ArrayList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
	}

	@Override
	public static <E extends Object> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10)
	{
		return ArrayList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
	}

	@Override
	public static <E extends Object> List<E> of(E[] elements)
	{
		return ArrayList.of(elements);
	}

	@Override
	public Stream stream()
	{
		return null;
	}

	@Override
	public Stream parallelStream()
	{
		return null;
	}
}
