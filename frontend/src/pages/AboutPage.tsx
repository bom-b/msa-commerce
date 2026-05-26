import styles from './AboutPage.module.scss'

export default function AboutPage() {
    return (
        <div className={styles.page}>
            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>프로젝트 소개</h1>
            </div>

            <div className={styles.infoBanner}>
                <p className={styles.infoBannerDesc}>
                    이 사이트는 MSA 아키텍처와 Apache Kafka의 이벤트 드리븐 패턴을 학습하기 위해 제작된 데모 프로젝트입니다.<br />
                    Spring Boot 기반의 마이크로서비스(주문·결제·재고)가 Kafka Choreography-based Saga 패턴으로 연동되어 있습니다.
                </p>
            </div>
        </div>
    )
}
