        stage('Package WAR') {
            steps {
                // 仅打包 docs-web 模块生成 docs-web-*.war，放到工作区供后续 Docker 构建使用
                sh 'mvn -B -DskipTests clean package -pl docs-web'
            }
        }
RUN sed -i 's|ports.ubuntu.com|archive.ubuntu.com|g' /etc/apt/sources.list && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -o Acquire::Retries=3 -y --no-install-recommends install \
        vim less procps unzip wget tzdata openjdk-11-jdk \
        ffmpeg \
        mediainfo \
        tesseract-ocr \
        tesseract-ocr-ara \
        tesseract-ocr-ces \
        tesseract-ocr-chi-sim \
        tesseract-ocr-chi-tra \
        tesseract-ocr-dan \
        tesseract-ocr-deu \
        tesseract-ocr-fin \
        tesseract-ocr-fra \
        tesseract-ocr-heb \
        tesseract-ocr-hin \
        tesseract-ocr-hun \
        tesseract-ocr-ita \
        tesseract-ocr-jpn \
        tesseract-ocr-kor \
        tesseract-ocr-lav \
        tesseract-ocr-nld \
        tesseract-ocr-nor \
        tesseract-ocr-pol \
        tesseract-ocr-por \
        tesseract-ocr-rus \
        tesseract-ocr-spa \
        tesseract-ocr-swe \
        tesseract-ocr-tha \
        tesseract-ocr-tur \
        tesseract-ocr-ukr \
        tesseract-ocr-vie \
        tesseract-ocr-sqi && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
