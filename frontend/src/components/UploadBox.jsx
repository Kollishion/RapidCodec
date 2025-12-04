export default function UploadBox({ onUpload }) {
  return (
    <div className="box">
      <h2>Upload Video</h2>
      <input
        type="file"
        accept="video/*"
        onChange={(e) => onUpload(e.target.files[0])}
      />
    </div>
  );
}
