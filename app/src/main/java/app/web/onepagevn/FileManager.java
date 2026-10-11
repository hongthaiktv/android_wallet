package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.*;
import android.provider.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class FileManager extends TreeSet<String>
{
	public static final int PICK_FILE = 1000;
	public static final int PICK_IMAGE = 1001;
	public static final int TYPE_JPEG = 1002;
	public static final int TYPE_PNG = 1003;
	public static final int TYPE_WEBP = 1004;
	public static final int TYPE_TEXT = 1005;
	public static final int TYPE_JSON = 1006;
	public static final int TYPE_MARKDOWN = 1007;
	public static final int TYPE_HTML = 1008;
	public static final int TYPE_OCTET_STREAM = 1009;
	
	private Context context;
	private StatusBar stt;
	private int pickerType = -1;
	private String dirPath = "";
	private Set<String> dirNames = new TreeSet<>(), fileNames = new TreeSet<>();
	private ContentResolver cr;
	
	public FileManager(Context context) {
		super();
		init(context, null);
	}
	
	public FileManager(Context context, String dirPath) {
		super();
		init(context, dirPath);
	}
	
	private void init(Context context, String dirPath) {
		this.context = context;
		this.dirPath = context.getFilesDir().getAbsolutePath();
		if (dirPath != null && !"".equals(dirPath)) {
			if (!dirPath.startsWith("/")) this.dirPath += "/" + dirPath.replaceFirst("/$", "");
			else this.dirPath = dirPath.replaceFirst("/$", "");
		}
		stt = new StatusBar("FileManager");
		cr = context.getContentResolver();
		refresh();
	}
	
	private void refresh() {
		clear(); dirNames.clear(); fileNames.clear();
		File directory = new File(dirPath);
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					add(file.getName());
					if (file.isDirectory()) dirNames.add(file.getName());
					else if (file.isFile()) fileNames.add(file.getName());
				}
			}
		}
		else stt.error("Wrong path or not an directory.");
	}
	
	public Set<String> list() {
		refresh();
		return this;
	}
	
	private Set<String> list(String dirPath, int returnType) {
		Set<String> allNames = new LinkedHashSet<>(), dirNames = new LinkedHashSet<>(), fileNames = new LinkedHashSet<>();
		if (dirPath == null || "".equals(dirPath)) dirPath = context.getFilesDir().getAbsolutePath();
		else if ("..".equals(dirPath)) {
			String directory = this.dirPath.replaceFirst("/$", "").replaceFirst("^.*/", "");
			String parentDir = this.dirPath.replaceFirst("/$", "").replaceFirst("/" + directory + "$", "");
			dirPath = parentDir;
		}
		else if (!dirPath.startsWith("/")) dirPath = this.dirPath.replaceFirst("/$", "") + "/" + dirPath.replaceFirst("/$", "");
		else dirPath = dirPath.replaceFirst("/$", "");
		File directory = new File(dirPath);
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					allNames.add(file.getName());
					if (file.isDirectory()) dirNames.add(file.getName());
					else if (file.isFile()) fileNames.add(file.getName());
				}
			}
			switch (returnType) {
				case 1: return dirNames;
				case 2: return fileNames;
				default: return allNames;
			}
		}
		else {
			stt.error("Wrong path or not an directory.");
			return null;
		}
	}
	
	public Set<String> list(String dirPath) {
		return list(dirPath, 0);
	}
	
	public Set<String> listDirs(String dirPath) {
		return list(dirPath, 1);
	}
	
	public Set<String> listFiles(String dirPath) {
		return list(dirPath, 2);
	}
	
	public FileManager setDirPath(String dirPath)
	{
		if (dirPath == null || "".equals(dirPath)) this.dirPath = context.getFilesDir().getAbsolutePath();
		else if ("..".equals(dirPath)) {
			String directory = this.dirPath.replaceFirst("/$", "").replaceFirst("^.*/", "");
			String parentDir = this.dirPath.replaceFirst("/$", "").replaceFirst("/" + directory + "$", "");
			this.dirPath = parentDir;
		}
		else if (!dirPath.startsWith("/")) this.dirPath = this.dirPath.replaceFirst("/$", "") + "/" + dirPath.replaceFirst("/$", "");
		else this.dirPath = dirPath.replaceFirst("/$", "");
		refresh();
		return this;
	}

	public String getDirPath()
	{
		return dirPath;
	}
	
	public Set<String> getDirNames()
	{
		return dirNames;
	}
	
	public Set<String> getFileNames()
	{
		return fileNames;
	}
	
	public void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
		String title = null;

		switch (requestCode) {
			case PICK_FILE:
				intent.setType("*/*");
				title = "Select File";
				pickerType = PICK_FILE;
				break;

			case PICK_IMAGE:
				intent.setType("image/*");
				title = "Select Picture";
				pickerType = PICK_IMAGE;
				break;

			default:
				pickerType = -1;
				stt.error("Wrong request code.");
				return;
		}
		((Activity) context).startActivityForResult(Intent.createChooser(intent, title), requestCode);
    }
	
	/*public Map<String, Object> saveFileToPrivateStorage(Uri uri) {
		
		
	 return fileInfo;
	}*/
	
	public Map<String, Object> saveImage(Uri uri) {
		if (pickerType == -1) {
			String msg = "Need open picker first";
			stt.error(msg);
			return null;
		}
		
		Map<String, Object> fileInfo = new HashMap<>();
		String dirName = "";
		switch (pickerType) {
			case PICK_FILE:
				dirName = "user_files";
				break;
			case PICK_IMAGE:
				dirName = "user_images";
				break;
		}
		
		String mime = cr.getType(uri);
		fileInfo.put("mime", mime);
		Bitmap.CompressFormat compressFormat = null;
		switch (mime) {
			case "image/jpeg":
				compressFormat = Bitmap.CompressFormat.JPEG;
				break;
			case "image/png":
				compressFormat = Bitmap.CompressFormat.PNG;
				break;
			case "image/webp":
				compressFormat = Bitmap.CompressFormat.WEBP;
				break;
			default:
				String msg = "Wrong image format.";
				stt.error(msg);
				return null;
		}
		
		try
		{
			Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex != -1 && sizeIndex != -1) {
                    String fileName = cursor.getString(nameIndex);
					fileInfo.put("name", fileName);
					File directory = dirPath.equals(context.getFilesDir().getAbsolutePath()) ? new File(context.getFilesDir(), dirName) : new File(dirPath);
					fileInfo.put("dirPath", directory.getAbsolutePath());
					fileInfo.put("directory", directory.getAbsolutePath().replaceFirst("/$", "").replaceFirst("^.*/", ""));
					if (!directory.exists()) directory.mkdirs();
					File file = new File(directory, fileName);
					String path = file.getAbsolutePath();
					fileInfo.put("path", path);
					fileInfo.put("size", cursor.getLong(sizeIndex));
					FileOutputStream fos = new FileOutputStream(file);
					Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
					boolean compressed = bitmap.compress(compressFormat, 100, fos);
					if (compressed) {
						if (dirPath.equals(directory.getAbsolutePath())) {
							add(fileName);
							fileNames.add(fileName);
						}
						stt.success("File \"" + fileName + "\" saved.");
					}
					else {
						stt.error("Saving file \"" + fileName + "\" failed.");
						cursor.close();
						return null;
					}
                }
            }
			if (cursor != null) cursor.close();
		}
		catch (Exception e)
		{stt.error(e.getMessage());}
		
		return fileInfo;
	}
	
	@Override
	public static <E extends Object> Set<E> of()
	{
		return TreeSet.of();
	}

	@Override
	public static <E extends Object> Set<E> of(E e1)
	{
		return TreeSet.of(e1);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2)
	{
		return TreeSet.of(e1, e2);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3)
	{
		return TreeSet.of(e1, e2, e3);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4)
	{
		return TreeSet.of(e1, e2, e3, e4);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5)
	{
		return TreeSet.of(e1, e2, e3, e4, e5);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6)
	{
		return TreeSet.of(e1, e2, e3, e4, e5, e6);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7)
	{
		return TreeSet.of(e1, e2, e3, e4, e5, e6, e7);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8)
	{
		return TreeSet.of(e1, e2, e3, e4, e5, e6, e7, e8);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9)
	{
		return TreeSet.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
	}

	@Override
	public static <E extends Object> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10)
	{
		return TreeSet.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
	}

	@Override
	public static <E extends Object> Set<E> of(E[] elements)
	{
		return TreeSet.of(elements);
	}

	@Override
	public Stream<String> stream()
	{
		return null;
	}

	@Override
	public Stream<String> parallelStream()
	{
		return null;
	}
}
