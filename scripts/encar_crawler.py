import re
import time
import random
import pymysql
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# MySQL 연결 설정
conn = pymysql.connect(
    host="localhost",
    user="encar_user",
    password="Mysql1234!",
    database="encar_db",
    charset="utf8mb4"
)
cursor = conn.cursor()

# Chromedriver 실행 파일 경로 설정
chromedriver_path = "/opt/homebrew/bin/chromedriver"

# Selenium 옵션 설정
chrome_options = Options()
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument("--headless")  # 백그라운드 실행
chrome_options.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.123 Safari/537.36")

# Selenium WebDriver 실행
service = Service(chromedriver_path)
driver = webdriver.Chrome(service=service, options=chrome_options)

# 크롤링할 엔카 검색 URL
base_urls = {
    "국산차": "http://www.encar.com/dc/dc_carsearchlist.do?carType=kor",
    "수입차": "http://www.encar.com/fc/fc_carsearchlist.do?carType=for"
}

# 차량 크롤링 실행
for car_type, base_url in base_urls.items():
    for page in range(1, 3):  # 🚀 2페이지까지만 크롤링
        search_url = f"{base_url}&page={page}"
        driver.get(search_url)
        time.sleep(random.uniform(2, 4))  # 요청 속도 조절

        # 차량 목록 가져오기 함수
        def get_car_list():
            try:
                return WebDriverWait(driver, 10).until(
                    EC.presence_of_element_located((By.ID, "sr_normal"))
                ).find_elements(By.TAG_NAME, "tr")
            except:
                return []  # 차량이 없을 경우 빈 리스트 반환

        car_rows = get_car_list()
        print(f"[{car_type}] {page}페이지 차량 개수: {len(car_rows)}")

        if not car_rows:  # 차량이 없는 경우 다음 페이지로 이동
            continue

        for i in range(len(car_rows)):
            try:
                # 최신 차량 목록 다시 가져오기 (핵심 수정)
                car_rows = get_car_list()
                if i >= len(car_rows):  # 인덱스 초과 방지
                    print(f"[경고] 차량 리스트 인덱스 초과 (i: {i}, 차량 개수: {len(car_rows)})")
                    break

                car = car_rows[i]  # 최신 리스트에서 차량 가져오기

                # 차량 정보 가져오기
                name_element = car.find_element(By.CSS_SELECTOR, "td.inf")
                raw_text = " ".join(name_element.text.strip().split("\n"))

                car_model_match = re.search(r"^[가-힣A-Za-z0-9\s\-\(\)\.]+", raw_text)
                car_model = car_model_match.group(0).strip() if car_model_match else "차종 정보 없음"

                if re.search(r"\s\d{2}$", car_model):
                    car_model = car_model.rsplit(" ", 1)[0]

                year_match = re.search(r"(\d{2}/\d{2}식|\d{2}/\d{2}식\(\d{2}년형\))", raw_text)
                year = year_match.group(0) if year_match else "연식 정보 없음"

                km_match = re.search(r"(\d{1,3}(?:,\d{3})*)km", raw_text)
                km = int(km_match.group(1).replace(",", "")) if km_match else None

                price_element = car.find_element(By.CSS_SELECTOR, "td.prc_hs strong")
                price_text = price_element.text.strip().replace(",", "").replace("원", "")
                price = int(price_text) if price_text.isdigit() else 0

                fuel_match = re.search(r"(가솔린|디젤|LPG|하이브리드|전기)", raw_text)
                fuel = fuel_match.group(0) if fuel_match else "연료 정보 없음"

                region_match = re.search(r"(서울|경기|부산|대구|대전|광주|울산|인천|강원|충청|전라|경상|제주)", raw_text)
                region = region_match.group(0) if region_match else "지역 정보 없음"

                link_element = car.find_element(By.TAG_NAME, "a")
                detail_url = link_element.get_attribute("href")

                # 차량 상세 페이지 이동
                driver.get(detail_url)
                time.sleep(random.uniform(2, 4))

                # 정확한 차량 메인 이미지 가져오기 (swiper-slide-active)
                try:
                    active_slide = WebDriverWait(driver, 5).until(
                        EC.presence_of_element_located((By.CSS_SELECTOR, "div.swiper-slide-active img"))
                    )
                    high_res_image_url = active_slide.get_attribute("src")
                except:
                    high_res_image_url = "이미지 없음"

                # 다시 메인 페이지로 복귀
                driver.execute_script("window.history.back()")

                # 페이지 로드 대기 (핵심 수정)
                WebDriverWait(driver, 10).until(
                    EC.presence_of_element_located((By.ID, "sr_normal"))
                )

                # MySQL에 저장할 데이터
                car_data = (car_type, car_model, year, km, price, fuel, region, detail_url, high_res_image_url)

                query = """
                INSERT INTO cars (car_type, model, year, mileage, price, fuel, region, url, image_url)
                SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s FROM DUAL
                WHERE NOT EXISTS (
                    SELECT 1 FROM cars 
                    WHERE model = %s AND year = %s AND mileage = %s AND price = %s AND fuel = %s
                )
                """
                cursor.execute(query, car_data + (car_model, year, km, price, fuel))
                conn.commit()

                print(f"[{car_type}] 저장 완료: {car_model}, {year}, {km}, {fuel}, {region}, {detail_url}, {high_res_image_url}")

            except Exception as e:
                print("차량 크롤링 오류 발생:", e)

# WebDriver 종료
driver.quit()

# MySQL 연결 종료
cursor.close()
conn.close()
print("크롤링 완료 및 데이터베이스 저장 완료!")