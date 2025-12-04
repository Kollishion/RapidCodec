import { useState } from 'react';
import DownloadButton from './components/DownloadButton';
import EncoderSettings from './components/EncoderSettings';
import MetricsPanel from './components/MetricsPanel';
import ProgressBar from './components/ProgressBar';
import UploadBox from './components/uploadBox';

export default function App() {
  const [file, setFile] = useState(null);
  const [settings, setSettings] = useState({});
  const [progress, setProgress] = useState(0);
  const [metrics, setMetrics] = useState(null);
  const [outputId, setOutputId] = useState(null);

  return (
    <div className="container">
      <h1>Video Compression Tool</h1>

      <UploadBox onUpload={setFile} />

      {file && <EncoderSettings onChange={setSettings} settings={settings} />}

      {file && <ProgressBar progress={progress} />}

      {metrics && <MetricsPanel metrics={metrics} />}

      {outputId && <DownloadButton id={outputId} />}
    </div>
  );
}
