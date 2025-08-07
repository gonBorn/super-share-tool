import React, { useState } from 'react';
import { useParams } from 'react-router-dom';

function UploadForm() {
    const [file, setFile] = useState<File | null>(null);
    const [progress, setProgress] = useState(0);
    const [uploading, setUploading] = useState(false);
    const { '*': path = '' } = useParams();

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            setFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!file) {
            alert('Please select a file to upload.');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('path', path);

        setUploading(true);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/upload', true);

        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percentComplete = (e.loaded / e.total) * 100;
                setProgress(percentComplete);
            }
        });

        xhr.onload = () => {
            setUploading(false);
            setFile(null);
            setProgress(0);
            // A bit of a hack to refresh the file browser
            window.location.reload();
        };

        xhr.onerror = () => {
            setUploading(false);
            alert('Upload failed.');
        };

        xhr.send(formData);
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <input type="file" className="form-control-file" onChange={handleFileChange} />
                </div>
                <button type="submit" className="btn btn-primary" disabled={uploading}>
                    {uploading ? `Uploading... ${progress.toFixed(2)}%` : 'Upload File'}
                </button>
            </form>
            {uploading && (
                <div className="progress mt-2">
                    <div
                        className="progress-bar"
                        role="progressbar"
                        style={{ width: `${progress}%` }}
                        aria-valuenow={progress}
                        aria-valuemin={0}
                        aria-valuemax={100}
                    ></div>
                </div>
            )}
        </div>
    );
}

export default UploadForm;
