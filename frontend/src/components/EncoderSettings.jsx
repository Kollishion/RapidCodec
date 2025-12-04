export default function EncoderSettings({ settings, onChange }) {
  const update = (field, value) => onChange({ ...settings, [field]: value });

  return (
    <div className="box">
      <h2>Encoding Settings</h2>

      <label>Speed / Quality Preset:</label>
      <select onChange={(e) => update('preset', e.target.value)}>
        <option value="fast">Fast</option>
        <option value="balanced">Balanced</option>
        <option value="ultra">Ultra</option>
      </select>

      <label>Choose Encoder:</label>
      <select onChange={(e) => update('encoder', e.target.value)}>
        <option value="baseline">Baseline</option>
        <option value="proposed">Proposed</option>
        <option value="proposed2">Proposed V2</option>
      </select>

      <label>
        <input
          type="checkbox"
          onChange={(e) => update('multithread', e.target.checked)}
        />
        Enable Multi-Threading
      </label>

      <label>
        <input
          type="checkbox"
          onChange={(e) => update('temporal', e.target.checked)}
        />
        Enable Temporal Cache
      </label>
    </div>
  );
}
