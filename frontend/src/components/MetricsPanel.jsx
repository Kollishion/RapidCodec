export default function MetricsPanel({ metrics }) {
  return (
    <div className="box">
      <h2>Encoding Metrics</h2>
      <p>Runtime: {metrics.runtime}s</p>
      <p>RDO Calls: {metrics.rdoCalls}</p>
      <p>Cache Hits: {metrics.cacheHits}</p>
      <p>Temporal Hits: {metrics.temporalHits}</p>
      <p>Compression Ratio: {metrics.ratio}</p>
      <p>PSNR: {metrics.psnr}</p>
      <p>SSIM: {metrics.ssim}</p>
    </div>
  );
}
