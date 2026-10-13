package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.*;
import android.provider.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import java.nio.file.Path;
import java.net.*;
import java.util.regex.*;

public class FileManager extends TreeSet<String>
{
	public static final int PICK_FILE = 1000;
	public static final int PICK_IMAGE = 1001;
	public static final int PICK_DIRECTORY = 1002;
	public static final int PICK_SAVETO = 1003;
	public static final int TYPE_JPEG = 1004;
	public static final int TYPE_PNG = 1005;
	public static final int TYPE_WEBP = 1006;
	public static final int TYPE_TEXT = 1007;
	public static final int TYPE_JSON = 1008;
	public static final int TYPE_MARKDOWN = 1009;
	public static final int TYPE_HTML = 1010;
	public static final int TYPE_OCTET_STREAM = 1011;
	
	private Context context;
	private StatusBar stt;
	private int pickerType = -1;
	private String dirPath = "";
	private Set<String> dirNames = new TreeSet<>(), fileNames = new TreeSet<>();
	private ContentResolver cr;
	private int imageCompressRatio = 100;
	private int bufferSize = 65536;
	private OnActivityResultListener onPickFileResultListener, onPickImageResultListener, onPickSaveToResultListener, onPickDirectoryResultListener;
	
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
			String parentDir = this.dirPath.replaceFirst("/$", "").replaceFirst("/" + Pattern.quote(directory) + "$", "");
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
			String parentDir = this.dirPath.replaceFirst("/$", "").replaceFirst("/" + Pattern.quote(directory) + "$", "");
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
	
	public void openFilePicker(int requestCode, String fileName) {
        Intent intent = new Intent();
		String title = null;

		switch (requestCode) {
			case PICK_FILE:
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				title = "Select File";
				pickerType = PICK_FILE;
				break;

			case PICK_IMAGE:
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				title = "Select Picture";
				pickerType = PICK_IMAGE;
				break;
				
			case PICK_SAVETO:
				intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				String mime = URLConnection.guessContentTypeFromName(fileName) != null ? URLConnection.guessContentTypeFromName(fileName) : "application/octet-stream";
				intent.setType(mime);
				intent.putExtra(Intent.EXTRA_TITLE, fileName);
				title = "Save To";
				pickerType = PICK_SAVETO;
				break;
				
			case PICK_DIRECTORY:
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
				title = "Select Directory";
				pickerType = PICK_DIRECTORY;
				break;
				
			default:
				pickerType = -1;
				stt.error("Wrong request code.");
				return;
		}
		((Activity) context).startActivityForResult(Intent.createChooser(intent, title), requestCode);
    }
	
	public void openFilePicker(int requestCode) {
		openFilePicker(requestCode, null);
	}
	
	private void writeFile(InputStream is, OutputStream os) throws IOException {
		boolean isOK = false;
		try {
			byte[] buffer = new byte[bufferSize];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			isOK = true;
		} catch (Exception e)
		{stt.log(e.getMessage());}
		try
		{
			is.close();
			os.close();
		}
		catch (Exception e)
		{stt.log(e.getMessage());}
		if (!isOK) throw new IOException("Write file failed.");
	}
	
	public Map<String, Object> saveFromUri(Uri uri) {
		if (pickerType != PICK_FILE) {
			String msg = "Need open file picker first";
			stt.error(msg);
			return null;
		}

		Map<String, Object> fileInfo = new HashMap<>();
		String dirName = "user_files";

		String mime = cr.getType(uri);
		fileInfo.put("mime", mime);
		
		try
		{
			File directory = new File(context.getFilesDir(), dirName);
			String fileName = "test.dump";
			File file = new File(directory, fileName);
			InputStream is = cr.openInputStream(uri);
			FileOutputStream fos = new FileOutputStream(file);
			writeFile(is, fos);
			stt.success("File \"" + fileName + "\" saved.");
		}
		catch (Exception e)
		{stt.error(e.getMessage());}

		return fileInfo;
	}
	
	public Map<String, Object> saveImageFromUri(Uri uri) {
		if (pickerType != PICK_IMAGE) {
			String msg = "Need open image picker first";
			stt.error(msg);
			return null;
		}
		
		Map<String, Object> fileInfo = new HashMap<>();
		String dirName = "user_images";
		
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
					fileInfo.put("directory", directory.getName());
					if (!directory.exists()) directory.mkdirs();
					File file = new File(directory, fileName);
					String path = file.getAbsolutePath();
					fileInfo.put("path", path);
					fileInfo.put("size", cursor.getLong(sizeIndex));
					InputStream is = cr.openInputStream(uri);
					FileOutputStream fos = new FileOutputStream(file);
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					boolean compressed = bitmap.compress(compressFormat, imageCompressRatio, fos);
					if (compressed) {
						if (dirPath.equals(directory.getAbsolutePath())) {
							add(fileName);
							fileNames.add(fileName);
						}
						stt.success("File \"" + fileName + "\" saved.");
					}
					else {
						stt.error("Saving file \"" + fileName + "\" failed.");
						is.close();
						fos.close();
						cursor.close();
						return null;
					}
					is.close();
					fos.close();
                }
            }
			if (cursor != null) cursor.close();
		}
		catch (Exception e)
		{stt.error(e.getMessage());}
		
		return fileInfo;
	}
	
	public Map<String, Object> saveToUri(File inputFile, Uri outputFile) {
		if (pickerType != PICK_SAVETO) {
			String msg = "Need open save picker first";
			stt.error(msg);
			return null;
		}

		Map<String, Object> fileInfo = new HashMap<>();
		String fileName = outputFile.getPath().replaceFirst("^.*/", "");
		long fileSize = inputFile.length();
		String mime = URLConnection.guessContentTypeFromName(fileName) != null ? URLConnection.guessContentTypeFromName(fileName) : "application/octet-stream";
		String dirName = outputFile.getPath().replaceFirst("/" + Pattern.quote(fileName) + "$", "").replaceFirst("^.*/", "");
		String dirPath = outputFile.toString().replaceFirst("(^.*" + Pattern.quote(dirName) + ").*", "$1");
		fileInfo.put("dirPath", dirPath);
		fileInfo.put("directory", dirName);
		fileInfo.put("name", fileName);
		fileInfo.put("mime", mime);
		fileInfo.put("path", outputFile.toString());
		
		try
		{
			InputStream is = new FileInputStream(inputFile);
			OutputStream os = cr.openOutputStream(outputFile);
			writeFile(is, os);
			fileInfo.put("size", fileSize);
			fileInfo.put("lastModified", new Date());
		}
		catch (Exception e) {
			stt.error(e.getMessage());
			return null;
		}
		return fileInfo;
	}
	
	public Uri takePersistableDirectory(Intent data) {
		if (pickerType != PICK_DIRECTORY) {
			String msg = "Need open directory picker first";
			stt.error(msg);
			return null;
		}
		
		Uri treeUri = data.getData();
		final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		cr.takePersistableUriPermission(treeUri, takeFlags);
		String dirID = DocumentsContract.getTreeDocumentId(treeUri);
		Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirID);
		return dirUri;
	}
	
	public Map<String, Object> saveToDir(File inputFile, Uri outputDir) {
		Map<String, Object> fileInfo = new HashMap<>();
		String fileName = inputFile.getName();
		long fileSize = inputFile.length();
		String mime = URLConnection.guessContentTypeFromName(fileName) != null ? URLConnection.guessContentTypeFromName(fileName) : "application/octet-stream";
		fileInfo.put("dirPath", outputDir.toString());
		fileInfo.put("directory", outputDir.getPath().replaceFirst("^.*/", ""));
		fileInfo.put("name", fileName);
		fileInfo.put("mime", mime);
		
		try
		{
			Uri fileUri = DocumentsContract.createDocument(cr, outputDir, mime, fileName);
			if (fileUri != null) {
				fileInfo.put("path", fileUri.toString());
				InputStream is = new FileInputStream(inputFile);
				OutputStream os = cr.openOutputStream(fileUri);
				writeFile(is, os);
				fileInfo.put("size", fileSize);
				fileInfo.put("lastModified", new Date());
			}
		}
		catch (Exception e) {
			stt.error(e.getMessage());
			return null;
		}
		return fileInfo;
	}
	
	public void setOnActivityResultListener(int requestCode, OnActivityResultListener listener) {
		switch (requestCode) {
			case PICK_FILE:
				onPickFileResultListener = listener;
				break;
			case PICK_IMAGE:
				onPickImageResultListener = listener;
				break;
			case PICK_SAVETO:
				onPickSaveToResultListener = listener;
				break;
			case PICK_DIRECTORY:
				onPickDirectoryResultListener = listener;
				break;
			default:
				stt.error("Wrong request code.");
		}
	}
	
	public void activityResultHandler(int requestCode, Intent data) {
		switch (requestCode) {
			case PICK_FILE:
				if (onPickFileResultListener != null) {
					onPickFileResultListener.onResult(this, data);
					onPickFileResultListener = null;
				}
				break;
			case PICK_IMAGE:
				if (onPickImageResultListener != null) {
					onPickImageResultListener.onResult(this, data);
					onPickImageResultListener = null;
				}
				break;
			case PICK_SAVETO:
				if (onPickSaveToResultListener != null) {
					onPickSaveToResultListener.onResult(this, data);
					onPickSaveToResultListener = null;
				}
				break;
			case PICK_DIRECTORY:
				if (onPickDirectoryResultListener != null) {
					onPickDirectoryResultListener.onResult(this, data);
					onPickDirectoryResultListener = null;
				}
				break;
			default:
				stt.error("Wrong request code.");
		}
	}
	
	public interface OnActivityResultListener {
		public void onResult(FileManager fm, Intent data);
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
