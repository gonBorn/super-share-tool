import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';

interface FileInfo {
    name: string;
    path: string;
    isDirectory: boolean;
    size: number;
}

function FileBrowser() {
    const [files, setFiles] = useState<FileInfo[]>([]);
    const { '*': path } = useParams();

    useEffect(() => {
        const fetchFiles = async () => {
            const url = path ? `/api/files/${path}` : '/api/files';
            try {
                const response = await fetch(url);
                if (response.ok) {
                    const data = await response.json();
                    setFiles(data);
                } else {
                    console.error('Failed to fetch files');
                }
            } catch (error) {
                console.error('Error fetching files:', error);
            }
        };
        fetchFiles();
    }, [path]);

    const formatBytes = (bytes: number, decimals = 2) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    };

    return (
        <div>
            <h2>File Browser</h2>
            <table className="table table-hover mt-4">
                <thead>
                    <tr>
                        <th>Type</th>
                        <th>Name</th>
                        <th>Size</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {files.map(file => (
                        <tr key={file.name}>
                            <td>{file.isDirectory ? '📁' : '📄'}</td>
                            <td>
                                {file.isDirectory ? (
                                    <Link to={`/browse/${file.path}`}>{file.name}</Link>
                                ) : (
                                    <a href={`/download/${file.path}`}>{file.name}</a>
                                )}
                            </td>
                            <td>{file.isDirectory ? '' : formatBytes(file.size)}</td>
                            <td>
                                {file.isDirectory && (
                                    <a href={`/download-zip/${file.path}`} className="btn btn-primary btn-sm">
                                        Download ZIP
                                    </a>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default FileBrowser;
