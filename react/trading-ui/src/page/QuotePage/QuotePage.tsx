import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Form, Input, Modal, Table, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import axios from "axios";
import NavBar from "../../component/NavBar/NavBar";
import type { Quote } from "../../types/trading";
import { createQuoteUrl, dailyListQuotesUrl } from "../../util/constants";
import "./QuotePage.scss";

export default function QuotePage() {
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [form] = Form.useForm<{ ticker: string }>();

  const api = useMemo(
    () =>
      axios.create({
        headers: { "Content-Type": "application/json" },
      }),
    []
  );

  const columns: ColumnsType<Quote> = [
    { title: "Ticker", dataIndex: "ticker", key: "ticker", sorter: (a, b) => a.ticker.localeCompare(b.ticker) },
    { title: "Last", dataIndex: "lastPrice", key: "lastPrice" },
    { title: "Bid", dataIndex: "bidPrice", key: "bidPrice" },
    { title: "Bid Size", dataIndex: "bidSize", key: "bidSize" },
    { title: "Ask", dataIndex: "askPrice", key: "askPrice" },
    { title: "Ask Size", dataIndex: "askSize", key: "askSize" },
  ];

  const getQuotes = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get<Quote[]>(dailyListQuotesUrl);
      setQuotes(response.data || []);
    } catch (err) {
      message.error("Failed to load quotes");
    } finally {
      setLoading(false);
    }
  }, [api]);

  useEffect(() => {
    void getQuotes();
  }, [getQuotes]);

  const onCreateQuote = async (values: { ticker: string }) => {
    const ticker = values.ticker.trim().toUpperCase();
    if (!ticker) return;

    setLoading(true);
    try {
      await api.post(`${createQuoteUrl}/${encodeURIComponent(ticker)}`, {});
      message.success(`Added quote: ${ticker}`);
      setIsModalVisible(false);
      form.resetFields();
      await getQuotes();
    } catch (err) {
      message.error("Failed to create quote");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="quote-page">
      <NavBar />
      <div className="quote-page-content">
        <div className="title">
          <span>Quotes</span>
          <Button type="primary" onClick={() => setIsModalVisible(true)}>
            Add Quote
          </Button>
        </div>

        <Table<Quote>
          rowKey="ticker"
          dataSource={quotes}
          columns={columns}
          pagination={false}
          loading={loading}
        />
      </div>

      <Modal
        title="Add Quote"
        okText="Submit"
        visible={isModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={onCreateQuote}>
          <Form.Item
            label="Ticker"
            name="ticker"
            rules={[
              { required: true, message: "Ticker is required" },
              {
                pattern: /^[A-Za-z.]{1,10}$/,
                message: "Ticker must be 1-10 letters (or '.')",
              },
            ]}
          >
            <Input placeholder="AAPL" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
