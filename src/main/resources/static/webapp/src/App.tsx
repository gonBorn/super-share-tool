import React from 'react';
import { Routes, Route } from 'react-router-dom';
import FileBrowser from './components/FileBrowser';
import ChatPanel from './components/ChatPanel';
import UploadForm from './components/UploadForm';

function App() {
  return (
    <div className="container mt-4">
      <div className="row">
        <div className="col-md-8">
          <h1>File Share</h1>
          <UploadForm />
          <Routes>
            <Route path="/" element={<FileBrowser />} />
            <Route path="/browse/*" element={<FileBrowser />} />
          </Routes>
        </div>
        <div className="col-md-4">
          <ChatPanel />
        </div>
      </div>
    </div>
  );
}

export default App;