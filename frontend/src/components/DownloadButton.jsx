export default function DownloadButton({ id }) {
  return (
    <a
      className="download-btn"
      href={`http://localhost:8080/download/${id}`}
      download
    >
      Download Output Video
    </a>
  );
}
