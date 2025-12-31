import React from 'react';
import { DotLottieReact } from '@lottiefiles/dotlottie-react';
import './LoadingOverlay.css';

const LoadingOverlay = () => {
    return (
        <div className="loading-overlay">
            <div className="loading-content">
                <div className="lottie-container">
                    <DotLottieReact
                        src="https://lottie.host/897a2bc8-dc6b-481d-b7b3-f1728677a47d/giR3l29pyS.lottie"
                        loop
                        autoplay
                    />
                </div>
            </div>
        </div>
    );
};

export default LoadingOverlay;
